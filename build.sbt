name := """childhoodmaltreatment"""
organization := "com.imknowles"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.13.1"

libraryDependencies += guice
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "5.0.0" % Test
libraryDependencies += "org.webjars" %% "webjars-play" % "2.8.0"
libraryDependencies += "org.webjars" % "bootstrap" % "4.4.1"
libraryDependencies += "org.webjars" % "d3js" % "5.9.7"
libraryDependencies += "org.webjars" % "jquery" % "3.4.1"
libraryDependencies += "org.webjars" % "popper.js" % "1.15.0"
libraryDependencies += "org.webjars" % "c3" % "0.6.6"
libraryDependencies += "org.webjars.npm" % "feather-icons" % "4.24.1"

// Adds additional packages into Twirl
//TwirlKeys.templateImports += "com.example.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "com.example.binders._"