name := "yangen"

version := "1.0"

scalaVersion := "2.11.8"

resolvers += "Typesafe repository" at "https://repo.typesafe.com/typesafe/releases/"
resolvers += "Scalaz Bintray Repo" at "http://dl.bintray.com/scalaz/releases"
resolvers += Resolver.sonatypeRepo("snapshots")
resolvers += Classpaths.sbtPluginReleases

libraryDependencies += "com.lihaoyi" %% "ammonite-ops" % "0.5.7"