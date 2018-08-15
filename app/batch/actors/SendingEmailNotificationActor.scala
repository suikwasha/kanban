package batch.actors

import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import akka.actor.{Actor, Cancellable}
import batch.notifications.EmailNotificationService
import models.silhouette.{User, UserRepository}
import models.task.{Task, TaskRepository}
import org.slf4j.LoggerFactory
import scala.concurrent.duration._
import scala.concurrent.{ExecutionContextExecutor, Future}

class SendingEmailNotificationActor(
  taskRepository: TaskRepository,
  userRepository: UserRepository,
  notificationService: EmailNotificationService
) extends Actor {

  import SendingEmailNotificationActor.Messages._

  implicit val ec: ExecutionContextExecutor = context.dispatcher

  private[this] val logger = LoggerFactory.getLogger(getClass)

  val intervalTask: Cancellable =
    context.system.scheduler.schedule(5 seconds, 1 hour, self, CheckTasks)

  override def preStart()= {
    logger.info("starting SendingEmailNotificationActor")

    super.preStart()
  }

  override def postStop: Unit = {
    intervalTask.cancel()

    super.postStop()
  }

  override def receive: Receive = {
    case Finished => {
      logger.info(s"sending email notification finished")
    }
    case CheckTasks => {
      logger.info(s"start sending email notification")
      checkTasks
    }
  }

  private[this] def checkTasks: Unit = {
    val now = LocalDateTime.now
    val tomorrow = now.plus(1, ChronoUnit.DAYS)
    (for {
      tasks <- taskRepository.find(now, tomorrow).map(_.groupBy(_.author))
      users <- Future.sequence(tasks.keySet.toSeq.map(userRepository.find))
      _ <- Future.sequence(users.flatten.flatMap(u => tasks.get(u.id).map(sendNotification(u, _))))
    } yield {
      self ! Finished
    }).recover {
      case t: Throwable =>
        logger.error("{}", t)
        self ! Finished
    }
  }


  private[this] def sendNotification(user: User, tasks: Seq[Task]): Future[Seq[Unit]] = {
    logger.info(s"$user $tasks")
    Future.sequence(tasks.map(notificationService.sendDeadlineNotification(user, _)))
  }
}

object SendingEmailNotificationActor {

  sealed trait Message

  object Messages {

    case object CheckTasks extends Message

    case object Finished extends Message
  }
}
