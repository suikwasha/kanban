package controllers.task.web

import com.mohiva.play.silhouette.api.Silhouette
import controllers.NavBar
import controllers.helpers.RedirectNotSignedInUsers
import javax.inject.Inject
import models.silhouette.{User, UserEnv}
import models.task.States.{Complete, InComplete, InProgress}
import models.task.{State, States, Task, TaskService}
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.I18nSupport
import play.api.mvc._

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

  def listTasks(filters: Option[Seq[String]]): Action[AnyContent] = Action.async { implicit request =>
    redirectNotSignedInUsers { user =>
      import ListTasksPage.Default

      taskService.findTasks(user.id).map(applyFilter(_, validateFilter(filters))).map { tasks =>
        Ok(views.html.task.list(Default.copy(tasks = tasks)))
      }
    }
  }

  def searchTasks: Action[AnyContent] = Action.async { implicit request =>
    redirectNotSignedInUsers { user =>
      SearchTaskForm.FormInstance.bindFromRequest.fold(badRequest, showMatchedTasks(user, _))
    }
  }

  private[this] def badRequest(invalidSearchForm: Form[SearchTaskForm])(implicit request: Request[AnyContent]): Future[Result] = {
    import ListTasksPage.Default

    Future.successful(
      BadRequest(
        views.html.task.list(
          Default.copy(navBar = Default.navBar.copy(searchTaskForm = invalidSearchForm))
        )
      )
    )
  }

  private[this] def showMatchedTasks(user: User, searchForm: SearchTaskForm)(implicit request: Request[AnyContent]): Future[Result] = {
    import ListTasksPage.Default

    taskService.findTasks(user.id, searchForm.titleIncludes).map { tasks =>
      Ok(
        views.html.task.list(
          Default.copy(
            navBar = Default.navBar.copy(searchTaskForm = SearchTaskForm.FormInstance.fill(searchForm)),
            tasks
          )
        )
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

  def fromString(value: String): Option[State] = States.All.find(_.toString == value)
}

object ListTasksController {

  case class ListTasksPage(
    navBar: NavBar,
    tasks: Seq[Task]
  )

  object ListTasksPage {

    val Default: ListTasksPage = ListTasksPage(NavBar(showMenu = true), Seq())
  }

  case class SearchTaskForm(titleIncludes: String)

  object SearchTaskForm {

    val FormInstance: Form[SearchTaskForm] = Form(
      mapping(
        "titleIncludes" -> nonEmptyText
      )(SearchTaskForm.apply)(SearchTaskForm.unapply)
    )
  }
}
