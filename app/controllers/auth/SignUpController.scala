package controllers.auth

import com.mohiva.play.silhouette.api.{LoginInfo, Silhouette}
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import javax.inject.Inject
import models.silhouette.{UserEnv, UserIdentityService}
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.I18nSupport
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

class SignUpController @Inject()(
  cc: ControllerComponents,
  userService: UserIdentityService,
  silhouette: Silhouette[UserEnv],
  cp: CredentialsProvider
)(
  implicit ec: ExecutionContext
) extends AbstractController(cc)
  with I18nSupport {

  import SignUpController._

  def get: Action[AnyContent] = silhouette.UnsecuredAction.async { implicit request: Request[AnyContent] =>
    Future.successful(Ok(views.html.auth.signup(SignUpForm.FormInstance)))
  }

  def post: Action[AnyContent] = silhouette.UnsecuredAction.async { implicit request: Request[AnyContent] =>
    SignUpForm.FormInstance.bindFromRequest.fold(
      e => Future.successful(Ok(views.html.auth.signup(e))),
      signUpForm => createUser(signUpForm)
    )
  }

  private[this] def createUser(form: SignUpForm) = {
    userService.retrieve(LoginInfo(CredentialsProvider.ID, form.name)).flatMap { userOpt =>
      if(userOpt.isEmpty) {
        userService.create(form.name, form.email, form.password).map(_ => Redirect(controllers.routes.HomeController.index()))
      } else {
        Future.successful(Redirect(routes.SignUpController.get()))
      }
    }
  }
}

object SignUpController {

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

}

