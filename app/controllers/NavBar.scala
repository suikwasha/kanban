package controllers

import models.task.{State, States}
import play.api.data.Form
import controllers.task.web.ListTasksController.SearchTaskForm

case class NavBar(
  showMenu: Boolean,
  stateFilter: Seq[State] = States.All,
  searchTaskForm: Form[SearchTaskForm] = SearchTaskForm.FormInstance
)
