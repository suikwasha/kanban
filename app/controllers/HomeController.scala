package controllers

import com.mohiva.play.silhouette.api.{HandlerResult, Silhouette}
import javax.inject._
import models.shilhouette.UserEnv
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class HomeController @Inject()(
  cc: ControllerComponents,
  silhouette: Silhouette[UserEnv]
)(
  implicit ec: ExecutionContext
) extends AbstractController(cc) {

  def index() = Action.async { implicit request =>
    silhouette.UserAwareRequestHandler { userAwareRequest =>
      Future.successful(HandlerResult(Ok, userAwareRequest.identity))
    }.map {
      case HandlerResult(_, Some(user)) => Ok(views.html.index(user.name))
      case HandlerResult(_, None) => Redirect(routes.SignInController.get)
    }

  }
}
