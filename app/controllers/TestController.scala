package controllers

import java.time.Instant
import java.time.temporal.{ChronoUnit, TemporalUnit}
import java.util.Date
import play.api.libs.mailer.{Email, MailerClient}

import javax.inject.Inject
import models.task.TaskRepository
import play.api.libs.mailer.MailerClient
import play.api.mvc.{AbstractController, ControllerComponents}

import scala.concurrent.{ExecutionContext, Future}

class TestController @Inject()(cc: ControllerComponents, tasks: TaskRepository, mailer: MailerClient)(implicit ec: ExecutionContext) extends AbstractController(cc)
{

  def bbb = Action.async { implicit request =>
    Future.successful(Ok(""))
  }

}
