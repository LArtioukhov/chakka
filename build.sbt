ThisBuild / version := "0.0.2"

ThisBuild / scalaVersion := "3.0.2"

ThisBuild / scalacOptions ++= Seq(
  "ALL",
//  "-Xfatal-warnings",
  "-deprecation",
  "-feature",
  "-unchecked",
  "-language:implicitConversions",
  "-language:higherKinds",
  "-language:existentials",
  "-language:postfixOps"
)

lazy val chakka = (project in file("."))
  .aggregate()
  .settings(onLoadMessage :=
              """
        | ** Welcome to the sbt build definition for chaotic akka! **
        """.stripMargin,
            name := "chakka"
  )

lazy val testKit = internalProject("testKit", Dependencies.TestKit)

lazy val storage = chakkaProject("storage",
                                 "storage",
                                 Dependencies.MongoDB,
                                 Dependencies.AkkaTypedActors
)

lazy val pool = chakkaProject("pool", "pool", Dependencies.AkkaTypedActors)

def chakkaProject(projectId: String,
                  moduleName: String,
                  additionalSettings: sbt.Def.SettingsDefinition*
): Project =
  Project(projectId, file(projectId))
    .settings(name := s"chakka-$moduleName")
    .settings(additionalSettings: _*)
    .dependsOn(testKit % Test)

def internalProject(projectId: String,
                    additionalSettings: sbt.Def.SettingsDefinition*
): Project =
  Project(projectId, file(projectId))
    .settings(name := s"chakka-$projectId", publish / skip := true)
    .settings(additionalSettings: _*)
