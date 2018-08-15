package utils

import org.scalatest.{AsyncWordSpec, BeforeAndAfterAll}
import play.api.inject.guice.GuiceApplicationBuilder

trait WithTestApplication {
  this: AsyncWordSpec with BeforeAndAfterAll =>

  val application = new GuiceApplicationBuilder()
    .configure(
      Map(
        "slick.dbs.default.profile" -> "slick.jdbc.H2Profile$",
        "slick.dbs.default.db.driver" -> "org.h2.Driver",
        "slick.dbs.default.db.url" -> "jdbc:h2:mem:play"
      )
    ).build()

  val injector = application.injector

  override def afterAll: Unit = application.stop()
}
