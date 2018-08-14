package batch

import akka.actor.{ActorSystem, Props}
import batch.actors.SendingEmailNotificationActor
import batch.notifications.EmailNotificationService
import javax.inject.Inject
import models.silhouette.UserRepository
import models.task.TaskRepository

class BatchStarter @Inject()(
  system: ActorSystem,
  taskRepository: TaskRepository,
  userRepository: UserRepository,
  notificationService: EmailNotificationService
) {

  system.actorOf(Props(classOf[SendingEmailNotificationActor], taskRepository, userRepository, notificationService))
}
