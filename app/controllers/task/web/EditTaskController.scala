package controllers.task.web

import java.time.{Instant, LocalDateTime, ZoneId}
import java.util.Date
import javax.inject.Inject
import com.mohiva.play.silhouette.api.Silhouette
import controllers.helpers.RedirectNotSignedInUsers
import models.silhouette.{User, UserEnv, UserId}
import models.task.States.{Complete, InComplete, InProgress}
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
        taskOpt.fold(Future.successful(NotFound("NOT_FOUND")))(showEditTask)
      }
    }
  }

  private[this] def showEditTask(task: Task)(implicit request: Request[AnyContent]): Future[Result] =
    Future.successful(
      Ok(
        views.html.task.edit(
          task.id,
          possibleStates,
          EditTaskForm.fromTask(task),
          None
        )
      )
    )

  private[this] def possibleStates = States.All.map(EditTaskForm.toString).map(t => t -> t)

  def editTask(id: Long): Action[AnyContent] =  Action.async { implicit request =>
    redirectNotSignedInUsers { user =>
      EditTaskForm.FormInstance.bindFromRequest.fold(
        e => Future.successful(BadRequest(views.html.task.edit(TaskId(id), possibleStates, e, Some(e.errors.mkString(","))))),
        saveTask(user, TaskId(id), _)
      )
    }
  }

  private[this] def saveTask(executor: User, targetId: TaskId, form: EditTaskForm)(implicit request: Request[AnyContent]): Future[Result] = {
    val newTask = form.toTask(targetId, executor.id)
    taskService.saveTask(executor.id, newTask).map { isSucceeded =>
      if(isSucceeded) {
        Redirect(controllers.task.web.routes.EditTaskController.editTask(targetId.value))
      } else {
        BadRequest("BAD_REQUEST")
      }
    }
  }
}

object EditTaskController {

  case class EditTaskForm(title: String, description: String, state: String, deadline: Option[LocalDateTime]) {
    import EditTaskForm._

    def toTask(id: TaskId, authorId: UserId): Task =
      fromString(state).map(Task(id, authorId, title, description, _, deadline)).getOrElse {
        throw new IllegalStateException(s"unknown state $state")
      }

    implicit private[this] def toDate(localDateTimeOpt: Option[LocalDateTime]): Option[Date] = {
      localDateTimeOpt.map { localDateTime =>
        Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant)
      }
    }
  }

  object EditTaskForm {

    def fromTask(task: Task): Form[EditTaskForm] =
      FormInstance.fill(EditTaskForm(task.title,task.description, toString(task.state), task.deadline))

    implicit private[this] def toLocalDateTime(dateOpt: Option[Date]): Option[LocalDateTime] = {
      dateOpt.map { date =>
        LocalDateTime.ofInstant(Instant.ofEpochMilli(date.getTime), ZoneId.systemDefault())
      }
    }

    def toString(state: State): String = {
      state match {
        case InComplete => "InComplete"
        case Complete => "Complete"
        case InProgress => "InProgress"
      }
    }

    def fromString(value: String): Option[State] = {
      value match {
        case "InComplete" => Some(InComplete)
        case "Complete" => Some(Complete)
        case "InProgress" => Some(InProgress)
        case _ => None
      }
    }

    private[this] val StateValidator = nonEmptyText.verifying(fromString(_).isDefined)

    val FormInstance = Form(
      mapping(
        "title" -> nonEmptyText,
        "description" -> nonEmptyText,
        "state" -> StateValidator,
        "deadline" -> optional(localDateTime("yyyy-MM-dd'T'HH:mm"))
      )(EditTaskForm.apply)(EditTaskForm.unapply)
    )
  }
}