import sbt._
import Keys._

object Dependencies {

  val AkkaVersion = "2.6.9"

  val TestKit = Seq(
    libraryDependencies := Seq(
      "org.scalatest"     %% "scalatest"                % "3.2.0", // Apache License 2.0
      "com.typesafe.akka" %% "akka-actor-testkit-typed" % AkkaVersion, // Apache License 2.0
      "com.typesafe.akka" %% "akka-slf4j"               % AkkaVersion,
      "ch.qos.logback"     % "logback-classic"          % "1.2.3", // Eclipse Public License 1.0
      "com.google.code.findbugs" % "jsr305" % "3.0.2" % Optional
    ))

  val AkkaTypedActors = Seq(
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion // Apache License 2.0
    ))

  val MongoDB = Seq(
    libraryDependencies ++= Seq(
      "org.mongodb.scala" %% "mongo-scala-driver" % "4.1.0" // ApacheV2
    ))

}
