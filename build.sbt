import sbtcrossproject.{crossProject, CrossType}
import scala.sys.process._

lazy val metaV = "4.0.0-M3"
lazy val pluginPath = Def.setting {
  (baseDirectory in ThisBuild).value / "sublime-text" / "accessible-scala"
}

lazy val nativeSettings = Seq(
  scalaVersion := "2.11.12",
  nativeGC := "immix"
)

lazy val cli = project
  .in(file("cli"))
  .settings(nativeSettings)
  .settings(
    moduleName := "accessible-scala-cli",
    inConfig(Compile)(artifactPath in nativeLink := pluginPath.value / "accessible-scala")
  )
  .enablePlugins(ScalaNativePlugin)
  .dependsOn(libNative, espeak, cliArgsNative)

lazy val cliArgs = crossProject(JVMPlatform, NativePlatform)
  .withoutSuffixFor(JVMPlatform)
  .crossType(CrossType.Pure)
  .in(file("cli-args"))
  .nativeSettings(nativeSettings)
  .settings(
    moduleName := "accessible-scala-cli-args",
    buildInfoKeys := Seq[BuildInfoKey](moduleName, version),
    buildInfoPackage := "build",
    libraryDependencies ++= Seq(
      "org.rogach" %%% "scallop" % "3.1.2",
      "com.lihaoyi" %%% "utest" % "0.6.3" % Test,
    )
  )
  .enablePlugins(BuildInfoPlugin)
  .dependsOn(lib)

lazy val cliArgsJVM = cliArgs.jvm
lazy val cliArgsNative = cliArgs.native

lazy val espeak = project
  .in(file("espeak"))
  .settings(
    moduleName := "accessible-scala-espeak",
  )
  .settings(nativeSettings)
  .enablePlugins(ScalaNativePlugin)

lazy val lib = crossProject(JSPlatform, JVMPlatform, NativePlatform)
  .withoutSuffixFor(JVMPlatform)
  .crossType(CrossType.Pure)
  .in(file("lib"))
  .nativeSettings(nativeSettings)
  .settings(
    moduleName := "accessible-scala",
    libraryDependencies += "org.scalameta" %%% "scalameta" % metaV
  )

lazy val libJVM = lib.jvm
lazy val libJS = lib.js
lazy val libNative = lib.native

lazy val testsShared = project
  .in(file("tests/shared"))
  .settings(
    libraryDependencies ++= Seq(
      "com.lihaoyi" %% "utest" % "0.6.3",
      "org.scalameta" %% "testkit" % metaV
    )
  )
  .dependsOn(libJVM)

lazy val unit = project
  .in(file("tests/unit"))
  .settings(
    libraryDependencies += "com.lihaoyi" %%% "fansi" % "0.2.5" % Test
  )
  .dependsOn(testsShared)

lazy val slow = project
  .in(file("tests/slow"))
  .settings(
    libraryDependencies += "me.tongfei" % "progressbar" % "0.5.5",
    javaOptions in (Test, test) ++= {
      val mem =
        if (sys.env.get("CI").isDefined) "4"
        else sys.env.get("SLOWMEM").getOrElse("20")

      List(
        "-Xss20m",
        "-Xms4G",
        s"-Xmx${mem}G",
        "-XX:ReservedCodeCacheSize=1024m",
        "-XX:+TieredCompilation",
        "-XX:+CMSClassUnloadingEnabled"
      )
    }
  )
  .dependsOn(testsShared)

lazy val webpackDir = Def.setting { (sourceDirectory in ThisProject).value / "webpack" }
lazy val webpackDevConf = Def.setting { Some(webpackDir.value / "webpack-dev.config.js") }
lazy val webpackProdConf = Def.setting { Some(webpackDir.value / "webpack-prod.config.js") }

lazy val scalajsSettings = Seq(
  scalacOptions += "-P:scalajs:sjsDefinedByDefault",
  useYarn := true,
  version in webpack := "3.5.5",
)

lazy val deployWeb = taskKey[Unit]("Deploy web demo")
def deployWebTask: Def.Initialize[Task[Unit]] = Def.task {
  // todo manual steps
  // created GitHub repo at https://github.com/scalacenter/accessible-scala-demo
  // activated GitHub Pages at https://github.com/scalacenter/accessible-scala-demo/settings
  // master branch

  // cd ..
  // cd accessible-scala-demo
  // git commit -am "."
  // git push origin master
}

lazy val web = project
  .in(file("web"))
  .settings(scalajsSettings)
  .settings(
    moduleName := "accessible-scala-demo",
    libraryDependencies ++= Seq(
      "org.scala-js" %%% "scalajs-dom" % "0.9.5"
    ),
    npmDependencies in Compile ++= Seq(
      "codemirror" -> "5.37.0",
      "firacode" -> "1.205.0",
      "mespeak" -> "2.0.2"
    ),
    npmDevDependencies in Compile ++= Seq(
      "compression-webpack-plugin" -> "1.0.0",
      "clean-webpack-plugin" -> "0.1.16",
      "css-loader" -> "0.28.5",
      "extract-text-webpack-plugin" -> "3.0.0",
      "file-loader" -> "0.11.2",
      "html-webpack-plugin" -> "2.30.1",
      "node-sass" -> "4.5.3",
      "resolve-url-loader" -> "2.1.0",
      "sass-loader" -> "6.0.6",
      "style-loader" -> "0.18.2",
      "uglifyjs-webpack-plugin" -> "0.4.6",
      "webpack-merge" -> "4.1.0"
    ),
    scalaJSUseMainModuleInitializer := true,
    deployWeb := deployWebTask.dependsOn(webpack in (Compile, fullOptJS)).value,
    version in startWebpackDevServer := "2.7.1",
    webpackConfigFile in fastOptJS := webpackDevConf.value,
    webpackConfigFile in fullOptJS := webpackProdConf.value,
    webpackMonitoredDirectories += (resourceDirectory in Compile).value,
    includeFilter in webpackMonitoredFiles := "*",
    webpackResources := webpackDir.value * "*.js",
    webpackBundlingMode in fastOptJS := BundlingMode.LibraryOnly(),
    webpackBundlingMode in fullOptJS := BundlingMode.Application,
    sourceGenerators in Compile += Def.task {
      import java.nio.file.{Paths, Files}
      import java.nio.charset.StandardCharsets

      val exampleFile = baseDirectory.value / "example" / "Example.scala"

      val example =
        new String(Files.readAllBytes(exampleFile.toPath), StandardCharsets.UTF_8)

      val file = (sourceManaged in Compile).value / "Example.scala"

      IO.write(
        file,
        s"""|package ch.epfl.scala.accessible
            |
            |object Example { 
            |  val code =
            |\"\"\"
            |$example
            |\"\"\"
            |}""".stripMargin
      )
      Seq(file)

    }.taskValue
  )
  .dependsOn(libJS)
  .enablePlugins(ScalaJSPlugin, ScalaJSBundlerPlugin)

lazy val installDependencies = Def.task[Unit] {
  val base = (baseDirectory in ThisProject).value
  val log = (streams in ThisProject).value.log
  if (!(base / "node_module").exists) {
    val pb =
      new java.lang.ProcessBuilder("npm", "install")
        .directory(base)
        .redirectErrorStream(true)

    pb ! log
  }
}

lazy val open = taskKey[Unit]("open vscode")
def openVSCodeTask: Def.Initialize[Task[Unit]] =
  Def
    .task[Unit] {
      val base = (baseDirectory in ThisProject).value
      val log = (streams in ThisProject).value.log

      val path = base.getCanonicalPath
      s"code --extensionDevelopmentPath=$path" ! log
      ()
    }
    .dependsOn(installDependencies)

lazy val publishMarketplace = taskKey[Unit]("publish vscode extension to marketplace")
def publishMarketplaceTask: Def.Initialize[Task[Unit]] =
  Def
    .task[Unit] {
      val ver = version.value
      val log = streams.value.log
      val pb =
        new java.lang.ProcessBuilder("bash", "-c", "./vsce publish")
          .directory(file("vscode"))
          .redirectErrorStream(true)
      pb ! log
      ()
    }
    .dependsOn(installDependencies)

lazy val vscode = project
  .in(file("vscode"))
  .settings(scalajsSettings)
  .settings(
    moduleName := "accessible-scala-vscode",
    scalacOptions += "-P:scalajs:sjsDefinedByDefault",
    scalaJSModuleKind := ModuleKind.CommonJSModule,
    artifactPath in (Compile, fastOptJS) := baseDirectory.value / "out" / "extension.js",
    artifactPath in (Compile, fullOptJS) := baseDirectory.value / "out" / "extension.js",
    open := openVSCodeTask.dependsOn(fastOptJS in Compile).value,
    publishMarketplace := publishMarketplaceTask.dependsOn(fullOptJS in Compile).value
  )
  .dependsOn(libJS)
  .enablePlugins(ScalaJSPlugin)
