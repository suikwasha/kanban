package controllers

import com.mohiva.play.silhouette.api.Silhouette
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import javax.inject.Inject
import models.shilhouette.UserEnv
import play.api.mvc.{AbstractController, AnyContent, ControllerComponents, Request}
import scala.concurrent.{ExecutionContext, Future}

class SignUpController @Inject()(
  cc: ControllerComponents,
  silhouette: Silhouette[UserEnv],
  cp: CredentialsProvider
)(
  ec: ExecutionContext
) extends AbstractController(cc) {

  def signUpPage = silhouette.UnsecuredAction.async { implicit request: Request[AnyContent] =>
    Future.successful(Ok(views.html.index()))
  }
}

