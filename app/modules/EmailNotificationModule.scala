package modules

import com.google.inject.name.Named
import com.google.inject.{AbstractModule, Inject, Provides}
import com.typesafe.config.Config
import net.codingwell.scalaguice.ScalaModule

class EmailNotificationModule @Inject()(
  config: Config
) extends AbstractModule with ScalaModule {

  override def configure(): Unit = {
  }

  @Provides
  @Named("EmailNotification-Sender")
  def providesEmailNotificationSender: String = {
    config.getString("kanban.notification.email.sender")
  }
}
