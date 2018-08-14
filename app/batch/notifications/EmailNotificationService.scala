package batch.notifications

import javax.inject.{Inject, Named}
import models.silhouette.User
import models.task.Task
import play.api.libs.mailer.{Email, MailerClient}
import scala.concurrent.{ExecutionContext, Future}

class EmailNotificationService @Inject()(
  mailer: MailerClient,
  @Named("EmailNotification-Sender") sender: String,
  sentEmailNotificationRepository: SentEmailNotificationRepository
)(
  implicit ec: ExecutionContext
){

  def sendDeadlineNotification(user: User, task: Task): Future[Unit] = {
    sentEmailNotificationRepository.find(task.id).map { senOpt =>
      if(senOpt.isEmpty) {
        user.email.foreach { email =>
          send(user, email, task)
        }
      }
    }
  }

  private[this] def send(user: User, email: String, task: Task): Future[SentEmailNotification] = {
    for {
      _ <- Future.successful(mailer.send(buildEmail(user, email, task)))
      sen <- sentEmailNotificationRepository.create(task.id)
    } yield sen
  }

  private[this] def buildEmail(user: User, email: String, task: Task): Email =
    Email(
      subject = s"kanban deadline notification (${task.title})",
      from = sender,
      to = Seq(email),
      bodyText = Some(s"Your Task ${task.title}'s deadline is approaching")
    )
}
