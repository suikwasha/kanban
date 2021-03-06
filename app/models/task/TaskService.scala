package models.task

import java.time.LocalDateTime
import javax.inject.Inject
import models.silhouette.UserId
import scala.concurrent.{ExecutionContext, Future}

class TaskService @Inject()(
  taskRepository: TaskRepository
)(
  implicit ec: ExecutionContext
){

  def findTask(user: UserId, id: TaskId): Future[Option[Task]] =
    taskRepository.find(id).map(_.filter(_.author == user))

  def findTasks(author: UserId): Future[Seq[Task]] =
    taskRepository.find(author)

  def findTasks(user: UserId, titleIncludes: String): Future[Seq[Task]] = {
    taskRepository.findByTitle(titleIncludes).map(_.filter(_.author == user))
  }

  def createTask(author: UserId, title: String, description: String, deadline: Option[LocalDateTime]): Future[Task] =
    taskRepository.create(author, title, description, States.InComplete, deadline)

  def deleteTask(user: UserId, taskId: TaskId): Future[Boolean] = {
    taskRepository.find(taskId).flatMap { taskOpt =>
      taskOpt.filter(_.author == user).fold(Future.successful(false))(t => taskRepository.delete(t.id))
    }
  }

  def saveTask(user: UserId, task: Task): Future[Boolean] = taskRepository.store(task)
}
