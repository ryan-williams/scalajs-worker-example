
scalaVersion in ThisBuild := "2.12.8"

lazy val app =
  project
    .enablePlugins(ScalaJSBundlerPlugin)
    .settings(
      libraryDependencies ++= Seq(
        "org.scala-js"  %%% "scalajs-dom"    % "0.9.6"
      ),
      scalaJSUseMainModuleInitializer := true,
      // Automatically generate index-dev.html which uses *-fastopt.js
      resourceGenerators in Compile += Def.task {
        val source = (resourceDirectory in Compile).value / "index.html"
        val target = (resourceManaged in Compile).value / "index-dev.html"

        val fullFileName = (artifactPath in (Compile, fullOptJS)).value.getName
        val fastFileName = (artifactPath in (Compile, fastOptJS)).value.getName

        IO.writeLines(target,
          IO.readLines(source).map {
            line => line.replace(fullFileName, fastFileName)
          }
        )

        Seq(target)
      }.taskValue
    )

lazy val worker =
  project
    .enablePlugins(ScalaJSPlugin)
    .dependsOn(app)
    .settings(
      scalaJSUseMainModuleInitializer := true
    )

lazy val root = project.aggregate(app, worker)
