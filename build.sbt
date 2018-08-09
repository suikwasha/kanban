name := """kanban"""
organization := "kanban"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.12.2"

libraryDependencies += guice
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % Test
libraryDependencies ++= Seq(
  "com.mohiva" %% "play-silhouette" % "5.0.0-RC2",
  "com.mohiva" %% "play-silhouette-password-bcrypt" % "5.0.0-RC2",
  "com.mohiva" %% "play-silhouette-persistence" % "5.0.0-RC2",
  "com.mohiva" %% "play-silhouette-crypto-jca" % "5.0.0-RC2",
  "com.mohiva" %% "play-silhouette-testkit" % "5.0.0-RC2" % "test"
)

resolvers += Resolver.jcenterRepo