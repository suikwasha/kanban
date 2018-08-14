package controllers.auth

import javax.inject.Inject
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}
import com.mohiva.play.silhouette.api.Silhouette
import models.silhouette.UserEnv
import scala.concurrent.ExecutionContext

class SignOutController @Inject()(
  cc: ControllerComponents,
  val silhouette: Silhouette[UserEnv]
)(
  implicit ec: ExecutionContext
) extends AbstractController(cc) {

  import silhouette._

  def signOut: Action[AnyContent] = SecuredAction.async { implicit request =>
    env.authenticatorService.discard(request.authenticator, Redirect(controllers.routes.HomeController.index()))
  }
}
