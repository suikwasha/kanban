package models.task

import com.google.inject.Inject
import models.shilhouette.UserId
import models.task.States.{Complete, InComplete}
import models.task.TaskRepository.{SlickTask, Tasks}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile
import slick.jdbc.MySQLProfile.api._

import scala.concurrent.{ExecutionContext, Future}

case class TaskId(value: Long)

case class Task(
  id: TaskId,
  author: UserId,
  title: String,
  description: String,
  state: State
)

sealed trait State

object States {

  val All = Seq(InComplete, Complete)

  case object InComplete extends State

  case object Complete extends State
}

class TaskRepository @Inject()(
  protected val dbConfigProvider: DatabaseConfigProvider
)(
  implicit ec: ExecutionContext
) extends HasDatabaseConfigProvider[JdbcProfile] {

  def find(authorId: UserId): Future[Seq[Task]] = db.run {
    TableQuery[Tasks]
      .filter(t => t.authorId === authorId.value)
      .result
  }.map(_.map(_.toTask))

  def create(author: UserId, title: String, description: String, state: State): Future[Task] = db.run {
    val tasks = TableQuery[Tasks]
    val insert = tasks.returning(tasks.map(_.id)).into((task, id) => task.copy(id = id))
    insert += SlickTask.fromTask(Task(TaskId(0), author, title, description, state))
  }.map(_.toTask)

  def store(task: Task): Future[Boolean] = db.run {
    val slickTask = SlickTask.fromTask(task)
    TableQuery[Tasks].filter(_.id === slickTask.id).update(slickTask)
  }.map(_ == 1)

}

object TaskRepository {

  case class SlickTask(id: Long, authorId: Long, title: String, description: String, state: String) {

    import SlickTask._

    def toTask = Task(
      id = TaskId(id),
      author = UserId(authorId),
      title = title,
      description = description,
      state = deserializeState(state)
    )

  }

  object SlickTask {

    def fromTask(from: Task): SlickTask = new SlickTask(
      id = from.id.value,
      authorId = from.author.value,
      title = from.title,
      description = from.description,
      state = serializeState(from.state)
    )

    private def deserializeState(stateStringRep: String): State = {
      stateStringRep match {
        case "in_complete" => InComplete
        case "complete" => Complete
        case _ => throw new IllegalStateException(s"unknown state $stateStringRep")
      }
    }

    private def serializeState(state: State): String = {
      state match {
        case InComplete => "in_complete"
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

    def * = (id, authorId, title, description, state) <> (SlickTask.tupled, SlickTask.unapply)

  }
}

