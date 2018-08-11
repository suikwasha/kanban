package models.task

import javax.inject.Inject
import models.shilhouette.UserId

import scala.concurrent.Future

class TaskService @Inject()(taskRepository: TaskRepository) {

  def findTasks(author: UserId): Future[Seq[Task]] =
    taskRepository.find(author)

  def createTask(author: UserId, title: String, description: String): Future[Task] =
    taskRepository.create(author, title, description, States.InComplete)

  def saveTask(task: Task): Future[Boolean] = taskRepository.store(task)
}
