package controllers

import com.mohiva.play.silhouette.api.Silhouette
import com.mohiva.play.silhouette.api.util.Credentials
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import javax.inject.Inject
import models.shilhouette.{UserEnv, UserIdentityService}
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, AnyContent, ControllerComponents, Request}
import play.api.data.Form
import play.api.data.Forms._

import scala.concurrent.{ExecutionContext, Future}

class SignInController @Inject()(
  cc: ControllerComponents,
  userService: UserIdentityService,
  silhouette: Silhouette[UserEnv],
  credentialsProvider: CredentialsProvider
)(
  implicit ec: ExecutionContext
) extends AbstractController(cc)
  with I18nSupport {

  def get = silhouette.UnsecuredAction.async { implicit request: Request[AnyContent] =>
    Future.successful(Ok(views.html.signin(SignInForm.FormInstance)))
  }

  def post = silhouette.UnsecuredAction.async { implicit request: Request[AnyContent] =>
    SignInForm.FormInstance.bindFromRequest.fold (
      e => Future.successful(BadRequest(views.html.signin(e))),
      signIn
    )
  }

  private[this] val authenticatorService = silhouette.env.authenticatorService

  private[this] def signIn(signInForm: SignInForm)(implicit request: Request[AnyContent]) = {
    (for {
      loginInfo <- credentialsProvider.authenticate(Credentials(signInForm.name, signInForm.password))
      authenticator <- authenticatorService.create(loginInfo)
      cookie <- authenticatorService.init(authenticator)
      result <- authenticatorService.embed(cookie, Redirect(routes.HomeController.index()))
    } yield {
      result
    }).recover {
      case e: Exception => {
        e.printStackTrace()
        Redirect(routes.SignUpController.get())
      }
    }
  }
}

case class SignInForm(name: String, password: String)

object SignInForm {

  val FormInstance = Form(
    mapping(
      "name" -> nonEmptyText,
      "password" -> nonEmptyText
    )(SignInForm.apply)(SignInForm.unapply)
  )
}
