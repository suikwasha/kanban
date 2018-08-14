package controllers.task

import com.mohiva.play.silhouette.api.Silhouette
import controllers.helpers.RedirectNotSignedInUsers
import javax.inject.Inject
import models.silhouette.UserEnv
import models.task.States.{Complete, InComplete}
import models.task.{State, States, Task, TaskService}
import play.api.mvc.{AbstractController, ControllerComponents}

import scala.concurrent.ExecutionContext

class ListTasksController @Inject()(
  cc: ControllerComponents,
  val silhouette: Silhouette[UserEnv],
  taskService: TaskService
)(
  implicit ec: ExecutionContext
) extends AbstractController(cc)
  with RedirectNotSignedInUsers {

  def listTasks(filters: Option[Seq[String]]) = Action.async { implicit request =>
    redirectNotSignedInUsers { user =>
      taskService.findTasks(user.id).map(applyFilter(_, validateFilter(filters))).map { tasks =>
        Ok(views.html.task.list(tasks))
      }
    }
  }

  private[this] def applyFilter(tasks: Seq[Task], filtersOpt: Option[Seq[State]]): Seq[Task] = {
    filtersOpt.map { filters =>
      if(filters.isEmpty) {
        tasks
      } else {
        tasks.filter(t => filters.contains(t.state))
      }
    }.getOrElse(tasks)
  }

  private[this] def validateFilter(filter: Option[Seq[String]]): Option[Seq[State]] = filter.map(_.flatMap(fromString))

  def fromString(value: String): Option[State] = {
    value match {
      case "InComplete" => Some(InComplete)
      case "Complete" => Some(Complete)
      case _ => None
    }
  }
}
