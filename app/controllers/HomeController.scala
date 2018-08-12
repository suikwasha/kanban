package controllers

import com.mohiva.play.silhouette.api.{HandlerResult, Silhouette}
import controllers.helpers.RedirectNotSignedInUsers
import javax.inject._
import models.shilhouette.{User, UserEnv}
import models.task.TaskService
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class HomeController @Inject()(
  cc: ControllerComponents,
  taskService: TaskService,
  val silhouette: Silhouette[UserEnv]
)(
  implicit ec: ExecutionContext
) extends AbstractController(cc)
  with RedirectNotSignedInUsers {

  def index = Action.async { implicit request =>
    redirectNotSignedInUsers(showTasks)
  }

  private[this] def showTasks(user: User)(implicit request: Request[AnyContent]): Future[Result] = {
    Future.successful(Redirect(controllers.task.routes.ListTasksController.listTasks(None)))
  }
}
