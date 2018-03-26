lazy val metaV = "3.6.0"

lazy val accesibleScala = project
  .in(file("accessible"))
  .settings(
    moduleName := "accesible-scala",
    assemblyJarName in assembly := "as.jar",
    mainClass.in(assembly) := Some("ch.epfl.scala.accessible.Main"),
    libraryDependencies ++= List(
      "com.lihaoyi" %% "pprint" % "0.5.2", // for debugging
      "org.scalameta" %% "scalameta" % metaV,
      "org.scalameta" %% "contrib" % metaV
    )
  )

lazy val testsShared = project
  .in(file("tests/shared"))
  .settings(
    libraryDependencies += "com.lihaoyi" %% "utest" % "0.6.3"
  )
  .dependsOn(accesibleScala)

lazy val unit = project
  .in(file("tests/unit"))
  .dependsOn(testsShared)
