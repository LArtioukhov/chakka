
ThisBuild / scalaVersion := "2.13.3"

ThisBuild / scalacOptions ++= Seq(
  "-Xelide-below", "ALL",
  "-Xfatal-warnings",
  "-deprecation",
  "-feature",
  "-unchecked",
  "-language:implicitConversions",
  "-language:higherKinds",
  "-language:existentials",
  "-language:postfixOps"
)

lazy val rootProject = (project in file("."))
  .settings(
    name := "chaoticAkka",
    version := "0.1"
  )