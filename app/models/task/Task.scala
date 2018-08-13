package models.task

import java.sql.Timestamp
import java.util.{Calendar, Date}
import com.google.inject.Inject
import models.shilhouette.UserId
import models.task.States.{Complete, InComplete, InProgress}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile
import slick.jdbc.H2Profile.api._
import scala.concurrent.{ExecutionContext, Future}

case class TaskId(value: Long)

case class Task(
  id: TaskId,
  author: UserId,
  title: String,
  description: String,
  state: State,
  deadline: Option[Date]
)

sealed trait State

object States {

  val All: Seq[State] = Seq(InComplete, InProgress, Complete)

  case object InComplete extends State {
    override def toString = "InComplete"
  }

  case object InProgress extends State {
    override def toString = "InProgress"
  }

  case object Complete extends State {
    override def toString = "Complete"
  }
}

class TaskRepository @Inject()(
  protected val dbConfigProvider: DatabaseConfigProvider,
)(
  implicit ec: ExecutionContext
) extends HasDatabaseConfigProvider[JdbcProfile] {

  import TaskRepository._
  import profile.api._

  def find(authorId: UserId): Future[Seq[Task]] = db.run {
    TableQuery[Tasks]
      .filter(t => t.authorId === authorId.value)
      .result
  }.map(_.map(_.toTask))

  def find(taskId: TaskId): Future[Option[Task]] = db.run {
    TableQuery[Tasks].filter(t => t.id === taskId.value).result.headOption
  }.map(_.map(_.toTask))

  def findByTitle(titleIncludes: String): Future[Seq[Task]] = db.run {
    val q = titleIncludes.flatMap(c => List('\\', c))
    TableQuery[Tasks].filter(t => t.title like s"%$q%").result
  }.map(_.map(_.toTask))

  def create(author: UserId, title: String, description: String, state: State, deadline: Option[Date]): Future[Task] = db.run {
    val tasks = TableQuery[Tasks]
    val insert = tasks.returning(tasks.map(_.id)).into((task, id) => task.copy(id = id))
    insert += SlickTask.fromTask(Task(TaskId(0), author, title, description, state, deadline))
  }.map(_.toTask)

  def store(task: Task): Future[Boolean] = db.run {
    val slickTask = SlickTask.fromTask(task)
    TableQuery[Tasks].filter(_.id === slickTask.id).update(slickTask)
  }.map(_ == 1)

  def delete(taskId: TaskId): Future[Boolean] = db.run {
    TableQuery[Tasks].filter(_.id === taskId.value).delete
  }.map(_ == 1)

}

object TaskRepository {

  case class SlickTask(id: Long, authorId: Long, title: String, description: String, state: String, deadline: Option[Timestamp]) {

    import SlickTask._

    def toTask = Task(
      id = TaskId(id),
      author = UserId(authorId),
      title = title,
      description = description,
      state = deserializeState(state),
      deadline = deadline
    )
  }

  implicit def toTimestamp(dateOpt: Option[Date]): Option[Timestamp] =
    dateOpt.map { date =>
      val cal = Calendar.getInstance
      cal.setTime(date)
      cal.set(Calendar.MILLISECOND, 0)
      new Timestamp(cal.getTimeInMillis)
    }

  implicit def toDate(timestampOpt: Option[Timestamp]): Option[Date] =
    timestampOpt.map(t => new Date(t.getTime))


  object SlickTask {

    def fromTask(from: Task): SlickTask = new SlickTask(
      id = from.id.value,
      authorId = from.author.value,
      title = from.title,
      description = from.description,
      state = serializeState(from.state),
      deadline = from.deadline
    )

    private def deserializeState(stateStringRep: String): State = {
      stateStringRep match {
        case "in_complete" => InComplete
        case "in_progress" => InProgress
        case "complete" => Complete
        case _ => throw new IllegalStateException(s"unknown state $stateStringRep")
      }
    }

    private def serializeState(state: State): String = {
      state match {
        case InComplete => "in_complete"
        case InProgress => "in_progress"
        case Complete => "complete"
      }
    }

    def tupled = (SlickTask.apply _).tupled
  }

  class Tasks(tag: Tag) extends Table[SlickTask](tag, "TASKS") {

    def id = column[Long]("ID", O.PrimaryKey, O.AutoInc)

    def authorId = column[Long]("AUTHOR_ID")

    def title = column[String]("TITLE")

    def description = column[String]("DESCRIPTION")

    def state= column[String]("STATE")

    def deadline = column[Option[Timestamp]]("DEADLINE")

    def * = (id, authorId, title, description, state, deadline) <> (SlickTask.tupled, SlickTask.unapply)

  }
}

