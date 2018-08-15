package modules.task

import java.time.LocalDateTime
import models.silhouette.UserId
import models.task.{States, TaskRepository}
import org.scalatest.{AsyncWordSpec, BeforeAndAfterAll}
import play.api.db.slick.DatabaseConfigProvider
import utils.WithTestApplication

class TaskRepositorySpec extends AsyncWordSpec with BeforeAndAfterAll with WithTestApplication {

  "TaskRepository" should {

    "able to create and find Task" in {
      val repo = new TaskRepository(injector.instanceOf[DatabaseConfigProvider])
      for {
        newTask <- repo.create(UserId(0), "title", "description", States.InComplete, None)
        createdTask <- repo.find(newTask.id)
      } yield {
        assert(createdTask.contains(newTask))
      }
    }

    "able to create and delete task" in {
      val repo = new TaskRepository(injector.instanceOf[DatabaseConfigProvider])
      for {
        newTask <- repo.create(UserId(0), "title", "description", States.InComplete, None)
        deleted <- repo.delete(newTask.id)
      } yield {
        assert(deleted)
      }
    }

    "able to find tasks with substring title" in {
      val repo = new TaskRepository(injector.instanceOf[DatabaseConfigProvider])
      for {
        newTask <- repo.create(UserId(0), "title1234", "description", States.InComplete, None)
        foundTask <- repo.findByTitle("1234")
      } yield {
        assert(foundTask.contains(newTask))
      }
    }

    "able to store task" in {
      val repo = new TaskRepository(injector.instanceOf[DatabaseConfigProvider])
      for {
        newTask <- repo.create(UserId(0), "title1234", "description", States.InComplete, None)
        isSuccess <- repo.store(newTask.copy(title = "title3456", description = "description2"))
        storedTask <- repo.find(newTask.id)
      } yield {

        assert(isSuccess && storedTask.exists(t => t.title == "title3456" && t.description == "description2"))
      }
    }

    "able to find tasks with date" in {
      val repo = new TaskRepository(injector.instanceOf[DatabaseConfigProvider])
      val deadline1 = LocalDateTime.of(2018, 1, 2, 12, 0, 0)
      val deadline2 = deadline1.plusDays(1)
      for {
        _ <- repo.create(UserId(0), "title", "description", States.InComplete, Some(deadline1))
        newTask2 <- repo.create(UserId(0), "title", "description", States.InComplete, Some(deadline2))
        foundTasks <- repo.find(deadline1.plusHours(1), deadline2)
      } yield {
        assert(foundTasks.contains(newTask2))
      }
    }
  }
}
