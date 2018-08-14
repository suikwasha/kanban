package controllers

import javax.inject.Inject
import play.api.mvc._
import play.api.data.Form
import play.api.data.Forms._
import scala.concurrent.{ExecutionContext, Future}
import com.mohiva.play.silhouette.api.services.AuthenticatorService
import com.mohiva.play.silhouette.api.{LoginInfo, Silhouette}
import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import models.silhouette.{UserEnv, UserIdentityService}

class SignUpController @Inject()(
  cc: ControllerComponents,
  userService: UserIdentityService,
  silhouette: Silhouette[UserEnv],
  cp: CredentialsProvider
)(
  implicit ec: ExecutionContext
) extends AbstractController(cc)
  with play.api.i18n.I18nSupport {

  val authService: AuthenticatorService[CookieAuthenticator] = silhouette.env.authenticatorService

  def get: Action[AnyContent] = silhouette.UnsecuredAction.async { implicit request: Request[AnyContent] =>
    Future.successful(Ok(views.html.signup(SignUpForm.FormInstance)))
  }

  def post: Action[AnyContent] = silhouette.UnsecuredAction.async { implicit request: Request[AnyContent] =>
    SignUpForm.FormInstance.bindFromRequest.fold(
      e => Future.successful(Ok(views.html.signup(e))),
      signUpForm => createUser(signUpForm)
    )
  }

  private[this] def createUser(form: SignUpForm) = {
    userService.retrieve(LoginInfo(CredentialsProvider.ID, form.name)).flatMap { userOpt =>
      if(userOpt.isEmpty) {
        userService.create(form.name, form.password).map(_ => Redirect(routes.HomeController.index()))
      } else {
        Future.successful(Redirect(routes.SignUpController.get()))
      }
    }
  }
}

case class SignUpForm(name: String, password: String, email: Option[String])

object SignUpForm {

  val FormInstance = Form(
    mapping(
      "name" -> nonEmptyText,
      "password" -> nonEmptyText,
      "email" -> optional(email)
    )(SignUpForm.apply)(SignUpForm.unapply)
  )

}

