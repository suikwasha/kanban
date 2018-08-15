package controllers.task.web

import java.time.{LocalDateTime, ZoneId}
import java.util.Date
import com.mohiva.play.silhouette.api.Silhouette
import controllers.NavBar
import controllers.helpers.RedirectNotSignedInUsers
import javax.inject.Inject
import models.silhouette.{User, UserEnv}
import models.task.TaskService
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.I18nSupport
import play.api.mvc._
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
      Future.successful(Ok(views.html.task.create(NavBar(showMenu = true), CreateTaskForm.FormInstance, None)))
    }
  }

  def createTask: Action[AnyContent] = Action.async { implicit request =>
    redirectNotSignedInUsers { user =>
      CreateTaskForm.FormInstance.bindFromRequest.fold(
        e => Future.successful(BadRequest(views.html.task.create(NavBar(showMenu = true), e, Some(e.errors.mkString(","))))),
        createTask(user, _)
      )
    }
  }

  private[this] def createTask(user: User, form: CreateTaskForm): Future[Result] = {
    for {
      newTask <- taskService.createTask(user.id, form.title, form.description, form.deadline)
    } yield {
      Redirect(controllers.task.web.routes.ListTasksController.listTasks(None))
    }
  }

  implicit private[this] def toDate(localDateTimeOpt: Option[LocalDateTime]): Option[Date] = {
    localDateTimeOpt.map { localDateTime =>
      Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant)
    }
  }
}

object CreateTaskController {

  case class CreateTaskForm(title: String, description: String, deadline: Option[LocalDateTime])

  object CreateTaskForm {

    val FormInstance = Form(
      mapping(
        "title" -> nonEmptyText,
        "description" -> nonEmptyText,
        "deadline" -> optional(localDateTime("yyyy-MM-dd'T'HH:mm"))
      )(CreateTaskForm.apply)(CreateTaskForm.unapply)
    )
  }

}

