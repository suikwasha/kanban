package controllers.task.api

import java.text.SimpleDateFormat
import java.time.{Instant, LocalDateTime, ZoneId}
import java.time.format.DateTimeFormatter
import java.util.Date

import com.mohiva.play.silhouette.api.Silhouette
import controllers.helpers.RedirectNotSignedInUsers
import javax.inject.Inject
import models.shilhouette.UserEnv
import models.task.States.InComplete
import models.task._
import play.api.libs.json._
import play.api.mvc.{AbstractController, ControllerComponents}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

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

  def getTask(id: Long) = Action.async { implicit request =>
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

  def putTask(id: Long) = Action.async(parse.json[TaskJsonRep]) { implicit reqeust =>
    Future.successful(Ok(Json.toJson(reqeust.body)))
  }

}

object TaskController {

  import Reads._

  case class TaskJsonRep(title: String, description: String, state: State, deadline: Option[LocalDateTime])

  object TaskJsonRep {

    def fromTask(task: Task): TaskJsonRep =
      new TaskJsonRep(task.title, task.description, task.state, task.deadline)
  }

  implicit private[this] def toLocalDateTime(dateOpt: Option[Date]): Option[LocalDateTime] = {
    dateOpt.map { date =>
      LocalDateTime.ofInstant(Instant.ofEpochMilli(date.getTime), ZoneId.systemDefault())
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
