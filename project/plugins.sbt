addSbtPlugin("com.typesafe.play"         % "sbt-plugin"                % "2.8.8")
addSbtPlugin("org.portable-scala"        % "sbt-scalajs-crossproject"  % "1.0.0")
addSbtPlugin("com.typesafe.sbt"          % "sbt-gzip"                  % "1.0.2")
addSbtPlugin("com.typesafe.sbt"          % "sbt-digest"                % "1.1.4")
// Breaking changes in 1.2.0 see https://github.com/vmunier/scalajs-scripts/issues/48 https://github.com/vmunier/play-scalajs.g8/pull/120/commits/ce22b7e7ac493cd47cf27845adb14a0ffb3123d9
addSbtPlugin("com.vmunier"               % "sbt-web-scalajs"           % "1.1.0")
addSbtPlugin("org.scala-js"              % "sbt-scalajs"               % "1.5.1")
addSbtPlugin("org.irundaia.sbt"          % "sbt-sassify"               % "1.5.1")

libraryDependencies += "org.vafer" % "jdeb" % "1.9" artifacts Artifact("jdeb", "jar", "jar")
