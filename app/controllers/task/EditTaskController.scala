package controllers.task

import com.mohiva.play.silhouette.api.Silhouette
import controllers.helpers.RedirectNotSignedInUsers
import javax.inject.Inject
import models.shilhouette.{User, UserEnv, UserId}
import models.task.States.{Complete, InComplete}
import models.task._
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.I18nSupport
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

class EditTaskController @Inject()(
  cc: ControllerComponents,
  taskService: TaskService,
  val silhouette: Silhouette[UserEnv]
)(
  implicit ec: ExecutionContext
) extends AbstractController(cc)
  with RedirectNotSignedInUsers
  with I18nSupport {

  import EditTaskController._

  def editTaskForm(id: Long): Action[AnyContent] = Action.async { implicit request =>
    redirectNotSignedInUsers { user =>
      taskService.findTask(user.id, TaskId(id)).flatMap { taskOpt =>
        taskOpt.fold(
          Future.successful(NotFound("NOT_FOUND"))
        )(
          showEditTask
        )
      }
    }
  }

  private[this] def showEditTask(task: Task)(implicit request: Request[AnyContent]): Future[Result] = {
    Future.successful(
      Ok(
        views.html.task.edit(
          task.id,
          possibleStates,
          EditTaskForm.fromTask(task)
        )
      )
    )
  }

  private[this] def possibleStates = States.All.map(EditTaskForm.toString).map(t => t -> t)

  def editTask(id: Long): Action[AnyContent] =  Action.async { implicit request =>
    redirectNotSignedInUsers { user =>
      EditTaskForm.FormInstance.bindFromRequest.fold(
        e => Future.successful(BadRequest(views.html.task.edit(TaskId(id), possibleStates, e))),
        saveTask(user, TaskId(id), _)
      )
    }
  }

  private[this] def saveTask(executor: User, targetId: TaskId, form: EditTaskForm)(implicit request: Request[AnyContent]): Future[Result] = {
    val newTask = form.toTask(targetId, executor.id)
    taskService.saveTask(executor.id, newTask).map { isSucceeded =>
      if(isSucceeded) {
        Ok(views.html.task.edit(targetId, possibleStates, EditTaskForm.fromTask(newTask)))
      } else {
        BadRequest("BAD_REQUEST")
      }
    }
  }
}

object EditTaskController {

  case class EditTaskForm(title: String, description: String, state: String) {
    import EditTaskForm._

    def toTask(id: TaskId, authorId: UserId): Task =
      fromString(state).map(Task(id, authorId, title, description, _)).getOrElse {
        throw new IllegalStateException(s"unknown state $state")
      }
  }

  object EditTaskForm {

    def fromTask(task: Task): Form[EditTaskForm] =
      FormInstance.bind(
        Map(
          "title" -> task.title,
          "description" -> task.description,
          "state" -> toString(task.state)
        )
      )

    def toString(state: State): String = {
      state match {
        case InComplete => "InComplete"
        case Complete => "Complete"
      }
    }

    def fromString(value: String): Option[State] = {
      value match {
        case "InComplete" => Some(InComplete)
        case "Complete" => Some(Complete)
        case _ => None
      }
    }

    private[this] val StateValidator = nonEmptyText.verifying(fromString(_).isDefined)

    val FormInstance = Form(
      mapping(
        "title" -> nonEmptyText,
        "description" -> nonEmptyText,
        "state" -> StateValidator
      )(EditTaskForm.apply)(EditTaskForm.unapply)
    )
  }
}