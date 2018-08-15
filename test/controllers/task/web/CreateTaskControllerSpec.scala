package controllers.task.web

import models.task.TaskRepository
import org.scalatest.{AsyncWordSpec, BeforeAndAfterAll}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import utils.{WithSignedInResult, WithTestApplication}

import scala.concurrent.Future

class CreateTaskControllerSpec
  extends AsyncWordSpec
  with BeforeAndAfterAll
  with WithTestApplication
  with WithSignedInResult {

  "CreateTaskController" should {

    "able to create new task" in {
      val target = injector.instanceOf[CreateTaskController]
      val reqTemplate = FakeRequest(POST, "/").withFormUrlEncodedBody(
        "title" -> "title123",
        "description" -> "description"
      )

      val tasks = injector.instanceOf[TaskRepository]

      for {
        signInResult <- signInResultFuture
        req <- Future.successful(reqTemplate.withCookies(signInResult.newCookies:_*))
        result <- target.createTask(req)
        task <- tasks.findByTitle("title123")
      } yield {
        assert(result.header.status == 303)
        assert(task.nonEmpty && task.headOption.exists(t => t.title == "title123" && t.description == "description"))
      }
    }
  }
}
