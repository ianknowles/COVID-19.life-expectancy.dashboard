import com.typesafe.sbt.packager.SettingsHelper.makeDeploymentSettings

/**build definition file
 *
 * JDK version must be 11 - 15.
 *
 * Play requires Java 1.8 or later. As of Play 2.8.8 Java 16 is not yet supported.
 * The build currently targets Java 11 as this is the current LTS version of Java and Play support for earlier versions
 * is incomplete.
 * Scala 3 and Java 17 (next LTS release) support is currently in development at Play.
 *
 * https://docs.scala-lang.org/overviews/jdk-compatibility/overview.html
 * https://www.playframework.com/documentation/latest/Requirements
 **/

ThisBuild / organization := "uk.co.imknowles"

ThisBuild / startYear := Some(2021)
ThisBuild / description := "Web Application scaffold for visualisation dashboards and dataset collections"

ThisBuild / scalaVersion := "2.13.6"
ThisBuild / version      := "1.0-SNAPSHOT"
ThisBuild / versionScheme := Some("early-semver")
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

val projectName: String = "graphing-app-scaffold"
lazy val root = (project in file("."))
	.aggregate(server, client, shared.jvm, shared.js)
	.settings(name := projectName)

val githubUser: String = "ianknowles"
val githubRepo: String = projectName
lazy val server = (project in file("server"))
	.settings(
		name := s"$projectName-server",
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
			"org.webjars" %% "webjars-play" % "2.8.8",
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
			"com.h2database" % "h2" % "1.4.200"
		),
		Linux / maintainer := "Ian Knowles <ian@imknowles.co.uk>",
		Linux / packageSummary := description.value,
		Linux / packageDescription := description.value,
		Debian / name := s"${name.value}-debian",
		Debian / debianPackageDependencies := Seq("openjdk-11-jre-headless"),
		Debian / debianPackageRecommends += "nginx",
		Universal / javaOptions ++= Seq(
			s"-Dpidfile.path=/var/run/${packageName.value}/play.pid",
			"-Dplay.evolutions.db.default.autoApply=true",
			"-Dplay.http.secret.key=APPLICATION_SECRET"
		),

		// publish to github packages
		publishTo := Some(s"GitHub $githubUser Apache Maven Packages" at s"https://maven.pkg.github.com/$githubUser/$githubRepo"),
		publishMavenStyle := true,
		credentials += Credentials(Path.userHome / ".sbt" / ".credentials"),
		makeDeploymentSettings(Debian, Debian / packageBin, "deb")
		//makeDeploymentSettings(Debian, Debian / genChanges, "changes")
	)
	.enablePlugins(PlayScala, JDebPackaging, SystemdPlugin, DebianDeployPlugin)
	.dependsOn(sharedJvm)

lazy val client = (project in file("client"))
	.settings(
		scalaJSUseMainModuleInitializer := true,
		libraryDependencies ++= Seq(
			"org.scala-js" %%% "scalajs-dom" % "1.1.0"
		),
		Universal / scalaJSStage := FullOptStage
	)
	.enablePlugins(ScalaJSPlugin, ScalaJSWeb)
	.dependsOn(sharedJs)

lazy val clientGraphing = (project in file("clientGraphing"))
	.settings(
		scalaJSUseMainModuleInitializer := true,
		libraryDependencies ++= Seq(
			"org.scala-js" %%% "scalajs-dom" % "1.1.0"
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

server / PlayKeys.devSettings += "play.server.http.port" -> "9001"

// loads the server project at sbt startup
Global / onLoad := (Global / onLoad).value.andThen(state => "project server" :: state)
