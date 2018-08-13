package controllers.task.api

import com.mohiva.play.silhouette.api.Silhouette
import controllers.helpers.RedirectNotSignedInUsers
import javax.inject.Inject
import models.shilhouette.UserEnv
import models.task.{Task, TaskId, TaskService}
import play.api.libs.json._
import play.api.mvc.{AbstractController, ControllerComponents}

import scala.concurrent.{ExecutionContext, Future}

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
          t => Ok(Json.toJson(t))
        )
      }
    }
  }
}

object TaskController {

  implicit val TaskWrites: Writes[Task] = (o: Task) => Json.obj(
    "title" -> o.title,
    "description" -> o.description,
    "state" -> o.state.toString,
    "deadline" -> o.deadline.map(_.toString)
  )
  
}
