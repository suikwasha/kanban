package batch.notifications

import models.task.TaskId
import com.google.inject.Inject
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile
import slick.jdbc.H2Profile.api._
import slick.lifted.ProvenShape

import scala.concurrent.{ExecutionContext, Future}

case class SentEmailNotificationId(value: Long)

case class SentEmailNotification(id: SentEmailNotificationId, taskId: TaskId)

class SentEmailNotificationRepository @Inject()(
  protected val dbConfigProvider: DatabaseConfigProvider
)(
  implicit ec: ExecutionContext
) extends HasDatabaseConfigProvider[JdbcProfile] {

  import SentEmailNotificationRepository._

  def find(taskId: TaskId): Future[Option[SentEmailNotification]] = db.run {
    TableQuery[SentEmailNotifications]
      .filter(_.taskId === taskId.value)
      .result
      .headOption
      .map(_.map(_.toSentEmailNotification))
  }

  def create(taskId: TaskId): Future[SentEmailNotification] = db.run {
    val sens = TableQuery[SentEmailNotifications]
    val insert = sens.returning(sens.map(_.id)).into((sen, id) => sen.copy(id = id))
    (insert += SlickSentEmailNotification(0, taskId.value)).map(_.toSentEmailNotification)
  }
}

object SentEmailNotificationRepository {

  case class SlickSentEmailNotification(id: Long, taskId: Long) {

    def toSentEmailNotification: SentEmailNotification =
      SentEmailNotification(SentEmailNotificationId(id), TaskId(taskId))
  }

  object SlickSentEmailNotification {

    def fromSentEmailNotification(sen: SentEmailNotification): SlickSentEmailNotification =
      new SlickSentEmailNotification(sen.id.value, sen.taskId.value)

    def tupled = (SlickSentEmailNotification.apply _).tupled
  }

  class SentEmailNotifications(tag: Tag) extends Table[SlickSentEmailNotification](tag, "SENT_EMAIL_NOTIFICATIONS") {

    def id: Rep[Long] = column[Long]("ID", O.PrimaryKey, O.AutoInc)

    def taskId: Rep[Long] = column[Long]("TASK_ID")

    def * : ProvenShape[SlickSentEmailNotification] = (id, taskId) <> (SlickSentEmailNotification.tupled, SlickSentEmailNotification.unapply)
  }
}
