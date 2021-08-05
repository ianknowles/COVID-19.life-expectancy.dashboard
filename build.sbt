ThisBuild / organization := "uk.co.imknowles"
ThisBuild / scalaVersion := "2.13.5"
ThisBuild / version      := "1.0-SNAPSHOT"
ThisBuild / scalacOptions ++= Seq(
	"-feature",
	"-deprecation",
	"-Xfatal-warnings",
	"-target:11"
)
ThisBuild / javacOptions ++= Seq("-source", "11", "-target", "11")

// Adds additional packages into Twirl
//TwirlKeys.templateImports += "com.example.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "com.example.binders._"

lazy val server = (project in file("server"))
	.settings(
		name := "graphing-server-core",
		scalaJSProjects := Seq(client, clientGraphing),
		Assets / pipelineStages := Seq(scalaJSPipeline),
		pipelineStages := Seq(digest, gzip),
		// triggers scalaJSPipeline when using compile or continuous compilation
		Compile / compile := ((Compile / compile) dependsOn scalaJSPipeline).value,
		libraryDependencies ++= Seq(
			"com.vmunier" %% "scalajs-scripts" % "1.1.4",
			guice,
			specs2 % Test,
			"org.scalatestplus.play" %% "scalatestplus-play" % "5.1.0" % Test,
			"org.webjars" %% "webjars-play" % "2.8.0",
			//"org.webjars" % "bootstrap" % "5.0.0",
			"org.webjars" % "bootstrap" % "4.6.0",
			"org.webjars.npm" % "bootstrap-icons" % "1.4.1",
			"org.webjars" % "d3js" % "6.6.0",
			"org.webjars.npm" % "topojson" % "3.0.2",
			"org.webjars" % "jquery" % "3.6.0",
			"org.webjars" % "popper.js" % "2.9.2",
			"org.webjars" % "c3" % "0.6.6",
			"com.typesafe.play" %% "play-slick" % "5.0.0",
			"com.typesafe.play" %% "play-slick-evolutions" % "5.0.0",
			"com.h2database" % "h2" % "1.4.200",
		),
		Linux / maintainer := "Ian Knowles <ian@imknowles.co.uk>",
		Linux / packageSummary := s"Webserver for ${name.value}",
		packageDescription := s"Webserver for ${name.value}",
		Debian / debianPackageDependencies := Seq("openjdk-11-jre-headless"),
		Debian / debianPackageRecommends += "nginx",
		Universal / javaOptions ++= Seq(
			s"-Dpidfile.path=/var/run/${packageName.value}/play.pid",
			"-Dplay.evolutions.db.default.autoApply=true",
			"-Dplay.http.secret.key=APPLICATION_SECRET"
		),
	)
	.enablePlugins(PlayScala, JDebPackaging, SystemdPlugin)
	.dependsOn(sharedJvm)

lazy val client = (project in file("client"))
	.settings(
		scalaJSUseMainModuleInitializer := true,
		libraryDependencies ++= Seq(
			"org.scala-js" %%% "scalajs-dom" % "1.1.0",
		),
		Universal / scalaJSStage := FullOptStage
	)
	.enablePlugins(ScalaJSPlugin, ScalaJSWeb)
	.dependsOn(sharedJs)

lazy val clientGraphing = (project in file("clientGraphing"))
	.settings(
		scalaJSUseMainModuleInitializer := true,
		libraryDependencies ++= Seq(
			"org.scala-js" %%% "scalajs-dom" % "1.1.0",
		),
		Universal / scalaJSStage := FullOptStage
	)
	.enablePlugins(ScalaJSPlugin, ScalaJSWeb)
	.dependsOn(sharedJs)

lazy val shared = crossProject(JSPlatform, JVMPlatform)
	.crossType(CrossType.Pure)
	.in(file("shared"))

lazy val sharedJvm = shared.jvm
lazy val sharedJs = shared.js

// loads the server project at sbt startup
Global / onLoad := (Global / onLoad).value.andThen(state => "project server" :: state)
