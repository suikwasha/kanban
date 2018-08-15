package modules.task

import models.silhouette.UserId
import models.task.States.InComplete
import models.task.{States, TaskId, TaskRepository, TaskService}
import org.scalatest.{AsyncWordSpec, BeforeAndAfterAll}
import utils.WithTestApplication

import scala.concurrent.Future

class TaskServiceSpec extends AsyncWordSpec with BeforeAndAfterAll with WithTestApplication {

  "TaskServiceSpec" should {

    "able to find task" in {
      val repo = injector.instanceOf[TaskRepository]
      val authorId = UserId(0)

      for {
        newTask <- repo.create(authorId, "title", "desc", States.InComplete, None)
        service <- Future.successful(new TaskService(repo))
        foundTask <- service.findTask(authorId, newTask.id)
      } yield {
        assert(foundTask.contains(newTask))
      }
    }

    "unable to find task created by other user" in {
      val repo = injector.instanceOf[TaskRepository]
      val authorId = UserId(0)
      val anothorAuthorId = UserId(1)

      for {
        newTask <- repo.create(authorId, "title", "desc", States.InComplete, None)
        service <- Future.successful(new TaskService(repo))
        foundTask <- service.findTask(anothorAuthorId, newTask.id)
      } yield {
        assert(foundTask.isEmpty)
      }
    }

    "able to find all tasks created by author" in {
      val repo = injector.instanceOf[TaskRepository]
      val authorId = UserId(0)

      for {
        newTask1 <- repo.create(authorId, "title", "desc", States.InComplete, None)
        newTask2 <- repo.create(authorId, "title", "desc", States.InComplete, None)
        service <- Future.successful(new TaskService(repo))
        foundTasks <- service.findTasks(authorId)
      } yield {

        assert(Seq(newTask1, newTask2).forall(foundTasks.contains))
      }
    }

    "create task with InComplete as initial state" in {
      val repo = injector.instanceOf[TaskRepository]
      val authorId = UserId(0)

      for {
        service <- Future.successful(new TaskService(repo))
        createdTask <- service.createTask(authorId, "title", "desc", None)
      } yield {

        assert(createdTask.state == InComplete)
      }
    }

    "able to delete task" in {
      val repo = injector.instanceOf[TaskRepository]
      val authorId = UserId(0)

      for {
        newTask1 <- repo.create(authorId, "title", "desc", States.InComplete, None)
        service <- Future.successful(new TaskService(repo))
        deleted <- service.deleteTask(authorId, newTask1.id)
      } yield {

        assert(deleted)
      }

    }
  }
}
