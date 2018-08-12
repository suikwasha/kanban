name := """kanban"""
organization := "kanban"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.12.2"

libraryDependencies += guice
libraryDependencies ++= Seq(
  "net.codingwell" %% "scala-guice" % "4.1.0"
)
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % Test
libraryDependencies ++= Seq(
  "com.mohiva" %% "play-silhouette" % "5.0.0-RC2",
  "com.mohiva" %% "play-silhouette-password-bcrypt" % "5.0.0-RC2",
  "com.mohiva" %% "play-silhouette-persistence" % "5.0.0-RC2",
  "com.mohiva" %% "play-silhouette-crypto-jca" % "5.0.0-RC2",
  "com.mohiva" %% "play-silhouette-testkit" % "5.0.0-RC2" % "test"
)
libraryDependencies ++= Seq(
  "com.typesafe.play" %% "play-slick" % "3.0.0",
  "com.typesafe.play" %% "play-slick-evolutions" % "3.0.0",
  "mysql" % "mysql-connector-java" % "6.0.6",
  "com.h2database" % "h2" % "1.4.197"
)
libraryDependencies ++= Seq(
  "com.adrianhurt" %% "play-bootstrap" % "1.4-P26-B4-SNAPSHOT"
)

resolvers += Resolver.jcenterRepo
resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"