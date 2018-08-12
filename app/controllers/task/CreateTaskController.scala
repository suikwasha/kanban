package controllers.task

import com.mohiva.play.silhouette.api.Silhouette
import controllers.helpers.RedirectNotSignedInUsers
import javax.inject.Inject
import models.shilhouette.{User, UserEnv}
import models.task.TaskService
import play.api.mvc._
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.I18nSupport

import scala.concurrent.{ExecutionContext, Future}

class CreateTaskController @Inject()(
  cc: ControllerComponents,
  taskService: TaskService,
  val silhouette: Silhouette[UserEnv]
)(
  implicit ec: ExecutionContext
) extends AbstractController(cc)
  with I18nSupport
  with RedirectNotSignedInUsers {

  import CreateTaskController._

  def createTaskForm: Action[AnyContent] = Action.async { implicit request =>
    redirectNotSignedInUsers { _ =>
      Future.successful(Ok(views.html.task.create(CreateTaskForm.FormInstance)))
    }
  }

  def createTask: Action[AnyContent] = Action.async { implicit request =>
    redirectNotSignedInUsers { user =>
      CreateTaskForm.FormInstance.bindFromRequest.fold(
        e => Future.successful(BadRequest(views.html.task.create(CreateTaskForm.FormInstance))),
        createTask(user, _)
      )
    }
  }

  private[this] def createTask(user: User, form: CreateTaskForm): Future[Result] = {
    for {
      newTask <- taskService.createTask(user.id, form.title, form.description)
    } yield {
      Redirect(controllers.task.routes.ListTasksController.listTasks(None))
    }
  }
}

object CreateTaskController {

  case class CreateTaskForm(title: String, description: String)

  object CreateTaskForm {

    val FormInstance = Form(
      mapping(
        "title" -> nonEmptyText,
        "description" -> nonEmptyText
      )(CreateTaskForm.apply)(CreateTaskForm.unapply)
    )
  }
}

