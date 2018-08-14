package controllers

import com.mohiva.play.silhouette.api.services.AuthenticatorService
import com.mohiva.play.silhouette.api.{LoginInfo, Silhouette}
import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import javax.inject.Inject
import models.silhouette.{UserEnv, UserIdentityService}
import play.api.mvc._
import scala.concurrent.{ExecutionContext, Future}
import play.api.data.Form
import play.api.data.Forms._

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

  def get= silhouette.UnsecuredAction.async { implicit request: Request[AnyContent] =>
    Future.successful(Ok(views.html.signup(SignUpForm.FormInstance)))
  }

  def post = silhouette.UnsecuredAction.async { implicit request: Request[AnyContent] =>
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

case class SignUpForm(name: String, password: String)

object SignUpForm {

  val FormInstance = Form(
    mapping(
      "name" -> nonEmptyText,
      "password" -> nonEmptyText
    )(SignUpForm.apply)(SignUpForm.unapply)
  )

}

