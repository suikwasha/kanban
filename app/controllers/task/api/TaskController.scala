package controllers.task.api

import java.time.{Instant, LocalDateTime, ZoneId}
import java.time.format.DateTimeFormatter
import java.util.Date
import com.mohiva.play.silhouette.api.Silhouette
import controllers.helpers.RedirectNotSignedInUsers
import javax.inject.Inject
import models.silhouette.{UserEnv, UserId}
import models.task._
import play.api.libs.json._
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}
import scala.concurrent.ExecutionContext
import scala.util.Try

class TaskController @Inject()
(
  cc: ControllerComponents,
  val silhouette: Silhouette[UserEnv],
  taskService: TaskService
)(
  implicit ec: ExecutionContext
) extends AbstractController(cc)
  with RedirectNotSignedInUsers
{
  import TaskController._

  def getTask(id: Long): Action[AnyContent] = Action.async { implicit request =>
    forbiddenNotSignedInUsers { user =>
      taskService.findTask(user.id, TaskId(id)).map { taskOpt =>
        taskOpt.fold(
          NotFound("{}")
        )(
          t => Ok(Json.toJson(TaskJsonRep.fromTask(t)))
        )
      }
    }
  }

  def putTask(id: Long): Action[TaskJsonRep] = Action.async(parse.json[TaskJsonRep]) { implicit request =>
    forbiddenNotSignedInUsers { user =>
      taskService.saveTask(user.id, request.body.toTask(TaskId(id), user.id)).map {
        case true => NoContent
        case false => BadRequest("BAD_REQUEST")
      }
    }
  }

}

object TaskController {

  import Reads._

  case class TaskJsonRep(title: String, description: String, state: State, deadline: Option[LocalDateTime]) {

    def toTask(id: TaskId, authorId: UserId): Task = {
      Task(
        id = id,
        author = authorId,
        title = title,
        description = description,
        state = state,
        deadline = toDate(deadline)
      )
    }
  }

  object TaskJsonRep {

    def fromTask(task: Task): TaskJsonRep =
      new TaskJsonRep(task.title, task.description, task.state, task.deadline)
  }

  implicit private def toLocalDateTime(dateOpt: Option[Date]): Option[LocalDateTime] = {
    dateOpt.map { date =>
      LocalDateTime.ofInstant(Instant.ofEpochMilli(date.getTime), ZoneId.systemDefault())
    }
  }

  implicit private def toDate(localDateTimeOpt: Option[LocalDateTime]): Option[Date] = {
    localDateTimeOpt.map { localDateTime =>
      Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant)
    }
  }

  implicit val TaskWrites: Writes[TaskJsonRep] = (o: TaskJsonRep) => Json.obj(
    "title" -> o.title,
    "description" -> o.description,
    "state" -> o.state.toString,
    "deadline" -> o.deadline
  )

  implicit val StateReads: Reads[State] = (json: JsValue) =>
    States.All.find(_.toString == json.as[String]).fold[JsResult[State]](JsError("undefined state"))(s => JsSuccess(s))

  implicit val LocalDateTimeReads: Reads[LocalDateTime] = (json: JsValue) =>
    Try(LocalDateTime.parse(json.as[String], DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")))
      .fold[JsResult[LocalDateTime]](
        _ => JsError(s"invalid format $json"),
        date => JsSuccess(date)
      )

  implicit val TaskReads: Reads[TaskJsonRep] = (json: JsValue) => {
    for {
      title <- (json \ "title").validate[String]
      description <- (json \ "description").validate[String]
      state <- (json \ "state").validate[State]
      deadline <- (json \ "deadline").validateOpt[LocalDateTime](LocalDateTimeReads)
    } yield TaskJsonRep(title, description, state, deadline)
  }
}
