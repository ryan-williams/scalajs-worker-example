
scalaVersion in ThisBuild := "2.12.8"

lazy val app =
  project
    .enablePlugins(ScalaJSBundlerPlugin)
    .settings(
      libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "0.9.6",
      scalaJSUseMainModuleInitializer := true
    )

lazy val worker =
  project
    .in(new File("app/worker"))
    .enablePlugins(ScalaJSPlugin)
    .dependsOn(app)
    .settings(
      scalaJSUseMainModuleInitializer := true
    )
