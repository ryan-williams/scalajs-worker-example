
scalaVersion in ThisBuild := "2.12.8"

lazy val app =
  project
    .enablePlugins(ScalaJSBundlerPlugin)
    .settings(
      libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "0.9.6",
      scalaJSUseMainModuleInitializer := true,
      resourceGenerators in Compile += Def.task {
        val resources =
          Seq(
            "index.html" → "index-dev.html",
            "worker.js" → "run-worker.js"
          )

        import IO._
        for {
          (source, target) ← resources
          src = (resourceDirectory in Compile).value / source
          dest = baseDirectory.value / target
          _ = writeLines(dest, readLines(src))
        } yield
          dest
      }
      .taskValue
    )

lazy val worker =
  project
    .in(new File("app/worker"))
    .enablePlugins(ScalaJSPlugin)
    .dependsOn(app)
    .settings(
      scalaJSUseMainModuleInitializer := true
    )
