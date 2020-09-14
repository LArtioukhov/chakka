ThisBuild / version := "0.0.1-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.3"

ThisBuild / scalacOptions ++= Seq(
  "-Xelide-below",
  "ALL",
//  "-Xfatal-warnings",
  "-deprecation",
  "-feature",
  "-unchecked",
  "-language:implicitConversions",
  "-language:higherKinds",
  "-language:existentials",
  "-language:postfixOps")

lazy val chakka = (project in file("."))
  .aggregate()
  .settings(
    onLoadMessage :=
      """
        | ** Welcome to the sbt build definition for chaotic akka! **
        """.stripMargin,
    name := "chakka")

lazy val testKit = internalProject("testKit", Dependencies.TestKit)

lazy val storage = chakkaProject("storage", "storage", Dependencies.MongoDB, Dependencies.AkkaTypedActors)

def chakkaProject(
    projectId: String,
    moduleName: String,
    additionalSettings: sbt.Def.SettingsDefinition*): Project =
  Project(projectId, file(projectId))
    .settings(name := s"chakka-$moduleName")
    .settings(additionalSettings: _*)
    .dependsOn(testKit % Test)

def internalProject(projectId: String, additionalSettings: sbt.Def.SettingsDefinition*): Project =
  Project(projectId, file(projectId))
    .settings(name := s"chakka-$projectId", publish / skip := true)
    .settings(additionalSettings: _*)
