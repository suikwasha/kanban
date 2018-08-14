package controllers.helpers

import play.api.mvc._
import com.mohiva.play.silhouette.api.{HandlerResult, Silhouette}
import models.silhouette.{User, UserEnv}
import scala.concurrent.{ExecutionContext, Future}

trait RedirectNotSignedInUsers {
  this: AbstractController =>

  def silhouette: Silhouette[UserEnv]

  def redirectNotSignedInUsers(
    block: User => Future[Result]
  )(
    implicit request: Request[AnyContent],
    ec: ExecutionContext
  ): Future[Result] =
    silhouette.UserAwareRequestHandler { userAwareRequest =>
      Future.successful(HandlerResult(Ok, userAwareRequest.identity))
    }.flatMap {
      case HandlerResult(_, Some(user)) => block(user)
      case HandlerResult(_, None) => Future.successful(Redirect(controllers.auth.routes.SignInController.get()))
    }

  def forbiddenNotSignedInUsers[T](
    block: User => Future[Result]
  )(
    implicit request: Request[T],
    ec: ExecutionContext
  ): Future[Result] =
    silhouette.UserAwareRequestHandler[T, User](request) { userAwareRequest =>
      Future.successful(HandlerResult(Ok, userAwareRequest.identity))
    }.flatMap {
      case HandlerResult(_, Some(user)) => block(user)
      case HandlerResult(_, None) => Future.successful(Forbidden("{}"))
    }

  def forbiddenNotSignedUsersAsync(
    block: (User, Request[AnyContent]) => Future[Result]
  )(
    implicit ec: ExecutionContext
  ): Action[AnyContent] = Action.async { implicit request =>
    forbiddenNotSignedInUsers { user =>
      block(user, request)
    }
  }

  def redirectNotSignedInUsersAsync(
    block: (User, Request[AnyContent]) => Future[Result]
  )(
    implicit ec: ExecutionContext
  ): Action[AnyContent] = Action.async { implicit request =>
    redirectNotSignedInUsers { user =>
      block(user, request)
    }
  }
}
