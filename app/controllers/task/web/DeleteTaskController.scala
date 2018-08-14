package controllers.task.web

import com.mohiva.play.silhouette.api.Silhouette
import controllers.helpers.RedirectNotSignedInUsers
import javax.inject.Inject
import models.silhouette.UserEnv
import models.task.{TaskId, TaskService}
import play.api.mvc.{AbstractController, ControllerComponents}

import scala.concurrent.ExecutionContext

class DeleteTaskController @Inject()(
  cc: ControllerComponents,
  val silhouette: Silhouette[UserEnv],
  taskService: TaskService
)(
  implicit ec: ExecutionContext
) extends AbstractController(cc)
  with RedirectNotSignedInUsers {

  def deleteTask(id: Long) = Action.async { implicit request =>
    redirectNotSignedInUsers { user =>
      taskService.deleteTask(user.id, TaskId(id)).map {
        case true => Redirect(controllers.routes.HomeController.index())
        case false => NotFound("NOT_FOUND")
      }
    }
  }
}
