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
  def aaa = Action.async { implicit request =>
    val now = Instant.now
    tasks.find(Date.from(now), Date.from(now.plus(1, ChronoUnit.DAYS))).map{ ts =>
      Ok(ts.toString)
    }
  }

  def bbb = Action.async { implicit request =>
    val mail = Email(
      subject = "subject",
      from = "suikwasha@gmail.com",
      to = Seq("tamaki.shoshi@gmail.com"),
      bodyText = Some("hogehogehoge")
    )

    val a = mailer.send(mail)
    Future.successful(Ok(a))
  }

}
