package batch.notifications

import com.typesafe.config.Config
import javax.inject.Inject

class EmailNotificationSenderProvider @Inject()(config: Config) {

  def get: String = config.getString("kanban.notification.email.sender")
}
