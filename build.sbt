name := """kanban"""

organization := "kanban"

scalaVersion := "2.12.2"

version := "1.0-SNAPSHOT"

lazy val root = (project in file("."))
    .enablePlugins(PlayScala)
    .settings(
      libraryDependencies ++= Seq(
        "net.codingwell" %% "scala-guice" % "4.1.0",
        "com.mohiva" %% "play-silhouette" % "5.0.0-RC2",
        "com.mohiva" %% "play-silhouette-password-bcrypt" % "5.0.0-RC2",
        "com.mohiva" %% "play-silhouette-persistence" % "5.0.0-RC2",
        "com.mohiva" %% "play-silhouette-crypto-jca" % "5.0.0-RC2",
        "com.mohiva" %% "play-silhouette-testkit" % "5.0.0-RC2" % "test",
        "com.typesafe.play" %% "play-mailer" % "6.0.1",
        "com.typesafe.play" %% "play-mailer-guice" % "6.0.1",
        "com.typesafe.play" %% "play-slick" % "3.0.0",
        "com.typesafe.play" %% "play-slick-evolutions" % "3.0.0",
        "mysql" % "mysql-connector-java" % "6.0.6",
        "com.h2database" % "h2" % "1.4.197",
        "com.adrianhurt" %% "play-bootstrap" % "1.4-P26-B4-SNAPSHOT",
        "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % "test",
        guice
      ),
      resolvers ++= Seq(
        Resolver.jcenterRepo,
        "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"
      )
    )
