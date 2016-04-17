
import sbt.Keys._

name          in ThisBuild := "yangen"
version       in ThisBuild := "1.0"
scalaVersion  in ThisBuild := "2.11.8"

resolvers += "Typesafe repository" at "https://repo.typesafe.com/typesafe/releases/"
resolvers += "Scalaz Bintray Repo" at "http://dl.bintray.com/scalaz/releases"
resolvers += "Twitter Maven" at "http://maven.twttr.com"
resolvers += Resolver.sonatypeRepo("snapshots")
resolvers += Classpaths.sbtPluginReleases

lazy val yangen = crossProject.in(file("."))
  .settings(
    libraryDependencies ++= Seq(
      "com.lihaoyi" %%% "upickle" % "0.3.9",
      "com.lihaoyi" %%% "autowire" % "0.2.5",
      "com.lihaoyi" %%% "scalatags" % "0.5.4",
      "com.lihaoyi" %%% "scalarx" % "0.3.1")
  )
  .jvmSettings(
    Revolver.settings : _*
  )
  .jvmSettings(
    libraryDependencies += "com.lihaoyi" %% "ammonite-ops" % "0.5.7"
  )
  .jsSettings(
    libraryDependencies ++= Seq(
      "org.scala-js" %%% "scalajs-dom" % "0.9.0",
      "com.timushev" %%% "scalatags-rx" % "0.1.0")
  )

lazy val yangenJs = yangen.js
lazy val yangenJvm = yangen.jvm
  .settings(
    (resources in Compile) += {
      (fastOptJS in (yangenJs, Compile)).value
      (artifactPath in (yangenJs, Compile, fastOptJS)).value
    }
  )