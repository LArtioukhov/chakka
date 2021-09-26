import sbt._
import sbt.Keys._

object Dependencies {

  val AkkaVersion = "2.6.16"

  val TestKit = Seq(
    libraryDependencies := Seq(
      "org.scalatest" %% "scalatest" % "3.2.9", // Apache License 2.0
      ("com.typesafe.akka" %% "akka-actor-testkit-typed" % AkkaVersion)
        .withCrossVersion(CrossVersion.for3Use2_13), // Apache License 2.0
      ("com.typesafe.akka" %% "akka-slf4j" % AkkaVersion)
        .withCrossVersion(CrossVersion.for3Use2_13),
      "ch.qos.logback" % "logback-classic" % "1.2.3", // Eclipse Public License 1.0
      "com.google.code.findbugs" % "jsr305" % "3.0.2" % Optional
    )
  )

  val AkkaTypedActors = Seq(
    libraryDependencies ++= Seq(
      ("com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion)
        .withCrossVersion(CrossVersion.for3Use2_13) // Apache License 2.0
    )
  )

  val MongoDB = Seq(
    libraryDependencies ++= Seq(
      ("org.mongodb.scala" %% "mongo-scala-driver" % "4.3.2")
        .withCrossVersion(CrossVersion.for3Use2_13) // ApacheV2
    )
  )

}
