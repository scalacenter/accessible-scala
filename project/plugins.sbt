addSbtPlugin(
  "io.get-coursier" % "sbt-coursier" % coursier.util.Properties.version)
addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.14.3")
addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.5.1")

addSbtPlugin("ch.jodersky" % "sbt-jni" % "1.2.6")