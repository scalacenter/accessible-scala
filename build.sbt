lazy val metaV = "3.6.0"

val pluginPath = Def.setting {
  (baseDirectory in ThisBuild).value / "plugins" / "sublime-text" / "accessible-scala"
}

val libraryPath = Def.setting {
  val targetDir = pluginPath.value / "bin"
  val libs = List(
    sys.env.get("ESPEAK_LIB_PATH"),
    Some(targetDir)
  ).flatten
  "-Djava.library.path=" + libs.mkString(":")
}

lazy val cli = project
  .in(file("cli"))
  .settings(
    fork in run := true,
    javaOptions in run += libraryPath.value,
    moduleName := "accessible-scala",
    mainClass.in(assembly) := Some("ch.epfl.scala.accessible.Main"),
    assemblyOutputPath in assembly := pluginPath.value / "ascala.jar"
  )
  .dependsOn(lib, espeak)

lazy val lib = project
  .in(file("lib"))
  .settings(
    libraryDependencies ++= List(
      "com.lihaoyi" %% "pprint" % "0.5.2", // for debugging
      "org.scalameta" %% "scalameta" % metaV,
      "org.scalameta" %% "contrib" % metaV
    )
  )

lazy val espeak = project
  .in(file("espeak"))
  .settings(
    target in nativeCompile := pluginPath.value,
    target in javah := (sourceDirectory in nativeCompile).value / "include",
    sourceDirectory in nativeCompile := sourceDirectory.value / "native"
  )
  .enablePlugins(JniNative)

lazy val testsShared = project
  .in(file("tests/shared"))
  .settings(
    libraryDependencies ++= Seq(
      "com.lihaoyi" %% "utest" % "0.6.3",
      "org.scalameta" %% "testkit" % metaV
    )
  )
  .dependsOn(lib) //, espeak)

lazy val unit = project
  .in(file("tests/unit"))
  .dependsOn(testsShared)
  .settings(
    fork in (Test, test) := true,
    fork in (Test, testOnly) := true,
    fork in (Test, testQuick) := true,
    cancelable in Global := true,
    javaOptions in (Test, test) += libraryPath.value,
    javaOptions in (Test, testOnly) ++= (javaOptions in (Test, test)).value,
    javaOptions in (Test, testQuick) ++= (javaOptions in (Test, test)).value
  )

lazy val slow = project
  .in(file("tests/slow"))
  .settings(
    libraryDependencies += "me.tongfei" % "progressbar" % "0.5.5",
    fork in (Test, test) := true,
    fork in (Test, testOnly) := true,
    fork in (Test, testQuick) := true,
    cancelable in Global := true,
    javaOptions in (Test, test) ++= {
      val mem =
        if (sys.env.get("CI").isDefined) "4"
        else sys.env.get("SLOWMEM").getOrElse("20")

      val libOptions = libraryPath.value

      libOptions :: List(
        "-Xss20m",
        "-Xms4G",
        s"-Xmx${mem}G",
        "-XX:ReservedCodeCacheSize=1024m",
        "-XX:+TieredCompilation",
        "-XX:+CMSClassUnloadingEnabled"
      )
    },
    javaOptions in (Test, testOnly) ++= (javaOptions in (Test, test)).value,
    javaOptions in (Test, testQuick) ++= (javaOptions in (Test, test)).value
  )
  .dependsOn(testsShared)
