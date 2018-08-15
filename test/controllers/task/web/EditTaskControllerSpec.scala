package controllers.task.web

import models.task.TaskRepository
import org.scalatest.{AsyncWordSpec, BeforeAndAfterAll}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import utils.{WithSignedInResult, WithTestApplication}

class EditTaskControllerSpec
  extends AsyncWordSpec
  with BeforeAndAfterAll
  with WithTestApplication
  with WithSignedInResult {

  "EditTaskController" should {

    "able to edit existing task" in {
      val createTaskController = injector.instanceOf[CreateTaskController]
      val reqTemplate = FakeRequest(POST, "/").withFormUrlEncodedBody(
        "title" -> "title123",
        "description" -> "description"
      )

      val tasks = injector.instanceOf[TaskRepository]

      val target = injector.instanceOf[EditTaskController]
      val editRequest = FakeRequest(POST, "/").withFormUrlEncodedBody(
        "title" -> "title345",
        "description" -> "description2",
        "state" -> "InProgress"
      )

      for {
        signInResult <- signInResultFuture
        _ <- createTaskController.createTask(reqTemplate.withCookies(signInResult.newCookies: _*))
        task <- tasks.findByTitle("title123")
        result <- target.editTask(task.head.id.value)(editRequest.withCookies(signInResult.newCookies: _*))
        newTask <- tasks.findByTitle("title345")
      } yield {
        assert(result.header.status == 303)
        assert(task.map(_.id) == newTask.map(_.id))
        assert(newTask.headOption.exists(t => t.title == "title345" && t.description == "description2"))
      }
    }
  }
}
