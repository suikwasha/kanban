/*
package models.task

import com.google.inject.Inject
import models.task.States.{Complete, InComplete}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile
import slick.jdbc.MySQLProfile.api._

case class UserId(value: Long)

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
) extends HasDatabaseConfigProvider[JdbcProfile] {



}

object TaskRepository {

  case class SlickTask(id: Long, authorId: Long, title: String, description: String, state: String) {

    def toTask
  }
}

class Tasks(tag: Tag) extends Table[Task](tag, "TASKS") {

  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

  def title = column[String]("title")

  def description = column[String]("description")

  def state= column[String]("state")

  def * = (id, title, description)

}

object Tasks {

  private def serialize(state: State): String =
    state match {
      case InComplete => "in_complete"
      case Complete => "complete"
    }

  private def deserialize(stateStringRep: String): Option[State] =
    stateStringRep match {
      case "in_complete" => Some(InComplete)
      case "complete" => Some(Complete)
      case _ => None
    }
}
*/
