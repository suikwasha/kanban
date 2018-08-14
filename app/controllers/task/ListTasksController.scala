package controllers.task

import com.mohiva.play.silhouette.api.Silhouette
import controllers.helpers.RedirectNotSignedInUsers
import javax.inject.Inject
import models.silhouette.UserEnv
import models.task.States.{Complete, InComplete}
import models.task.{State, States, Task, TaskService}
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, ControllerComponents}

import scala.concurrent.{ExecutionContext, Future}

class ListTasksController @Inject()(
  cc: ControllerComponents,
  val silhouette: Silhouette[UserEnv],
  taskService: TaskService
)(
  implicit ec: ExecutionContext
) extends AbstractController(cc)
  with I18nSupport
  with RedirectNotSignedInUsers {

  import ListTasksController._

  def listTasks(filters: Option[Seq[String]]) = Action.async { implicit request =>
    redirectNotSignedInUsers { user =>
        taskService.findTasks(user.id).map(applyFilter(_, validateFilter(filters))).map { tasks =>
          Ok(views.html.task.list(tasks))
      }
    }
  }

  def searchTasks = Action.async { implicit request =>
    redirectNotSignedInUsers { user =>
      SearchTaskForm.FormInstance.bindFromRequest.fold(
        e => Future.successful(Redirect(controllers.task.routes.ListTasksController.listTasks(None))),
        t => {
          taskService.findTasks(user.id, t.titleIncludes).map { tasks =>
            Ok(views.html.task.list(tasks))
          }
        }
      )
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

object ListTasksController {

  case class SearchTaskForm(titleIncludes: String)

  object SearchTaskForm {

    val FormInstance = Form(
      mapping(
        "titleIncludes" -> nonEmptyText
      )(SearchTaskForm.apply)(SearchTaskForm.unapply)
    )
  }
}
