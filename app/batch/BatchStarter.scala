package batch

import akka.actor.{ActorSystem, Props}
import batch.actors.SendingEmailNotificationActor
import javax.inject.Inject
import models.silhouette.UserRepository
import models.task.TaskRepository
import play.api.libs.mailer.MailerClient

class BatchStarter @Inject()(
  system: ActorSystem,
  taskRepository: TaskRepository,
  userRepository: UserRepository,
  mailerClient: MailerClient
) {

  system.actorOf(Props(classOf[SendingEmailNotificationActor], taskRepository, userRepository, mailerClient))
}
