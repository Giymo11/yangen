
import sbt.Keys._
import sbt.Project.projectToRef

name          in ThisBuild := "yangen"
version       in ThisBuild := "1.0"
val scalaV = "2.11.8"

lazy val http4sVersion = "0.13.2a"

resolvers += "Typesafe repository" at "https://repo.typesafe.com/typesafe/releases/"
resolvers += "Scalaz Bintray Repo" at "http://dl.bintray.com/scalaz/releases"
resolvers += "Twitter Maven" at "http://maven.twttr.com"
resolvers += Resolver.sonatypeRepo("snapshots")
resolvers += Classpaths.sbtPluginReleases

lazy val shared = crossProject.crossType(CrossType.Pure).in(file("shared"))
  .settings(
    name                := "shared",
    scalaVersion        := scalaV,
    libraryDependencies ++= Seq(
      "com.lihaoyi"     %%% "upickle"             % "0.3.9",
      "com.lihaoyi"     %%% "autowire"            % "0.2.5",
      "com.lihaoyi"     %%% "scalatags"           % "0.5.4",
      "com.lihaoyi"     %%% "scalarx"             % "0.3.1")
  )

lazy val sharedJvm = shared.jvm
  .settings(
    scalaVersion        := scalaV,
    name                := "sharedJvm"
  )

lazy val sharedJs = shared.js
  .settings(
    scalaVersion        := scalaV,
    name                := "sharedJs"
  )

lazy val cli = project.in(file("cli"))
  .settings(
    scalaVersion        := scalaV,
    name                := "cli",
    libraryDependencies ++= Seq(
      "com.lihaoyi"     %% "ammonite-ops"         % "0.5.7")
  ).dependsOn(sharedJvm)

lazy val jsUi = project.in(file("jsui"))
  .settings(
    scalaVersion        := scalaV,
    name                := "yangenJsUi",
    libraryDependencies ++= Seq(
      "org.scala-js"    %%% "scalajs-dom"         % "0.9.0",
      "com.timushev"    %%% "scalatags-rx"        % "0.1.0"),
    skip.in(packageJSDependencies) := false,
    persistLauncher := true
  )
  .enablePlugins(ScalaJSPlugin)
  .dependsOn(sharedJs)

lazy val server = project.in(file("server"))
  .settings(
    scalaVersion        := scalaV,
    name                := "yangenHttp4s",
    libraryDependencies ++= Seq(
      "org.http4s"      %% "http4s-dsl"           % http4sVersion,
      "org.http4s"      %% "http4s-blaze-server"  % http4sVersion,
      "org.http4s"      %% "http4s-blaze-client"  % http4sVersion)
  ).settings(
    Revolver.settings : _*
  ).dependsOn(sharedJvm)

lazy val yangenRoot = project.in(file("."))
  .aggregate(sharedJvm, sharedJs, cli, jsUi, server)