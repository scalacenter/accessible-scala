import sbt.Keys._
import sbt._
import sbt.plugins.JvmPlugin

object AccessibleScalaBuild extends AutoPlugin {
  override def trigger: PluginTrigger = allRequirements
  override def requires: Plugins = JvmPlugin
  override def globalSettings: Seq[Def.Setting[_]] = List(
    version := "0.2.0",
    scalaVersion := "2.12.6",
    scalacOptions ++= Seq(
      "-Xlint",
      "-deprecation",
      "-encoding",
      "UTF-8",
      "-feature",
      "-unchecked" //,
      // "-Ywarn-unused-import",
      // "-Ywarn-unused:locals",
      // "-Ywarn-unused:privates"
    ),
    testFrameworks := List(new TestFramework("utest.runner.Framework")),
    licenses := Seq(
      "Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")
    ),
    developers := List(
      Developer(
        "MasseGuillaume",
        "Guillaume Massé",
        "masgui@gmail.com",
        url("https://github.com/MasseGuillaume")
      )
    ),
    scmInfo in ThisBuild := Some(
      ScmInfo(
        url("https://github.com/scalacenter/accessible-scala"),
        s"scm:git:git@github.com:scalacenter/accessible-scala.git"
      )
    )
  )

}
