package controllers

import com.mohiva.play.silhouette.api.{HandlerResult, Silhouette}
import controllers.helpers.RedirectNotSignedInUsers
import javax.inject._
import models.silhouette.{User, UserEnv}
import models.task.TaskService
import play.api.i18n.I18nSupport
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
  with RedirectNotSignedInUsers
  with I18nSupport {

  def index = Action.async { implicit request =>
    redirectNotSignedInUsers(showTasks)
  }

  private[this] def showTasks(user: User)(implicit request: Request[AnyContent]): Future[Result] = {
    Future.successful(Redirect(controllers.task.routes.ListTasksController.listTasks(None)))
  }

  def test = Action.async { implicit request =>
    Future.successful(Ok(views.html.index()))
  }
}
