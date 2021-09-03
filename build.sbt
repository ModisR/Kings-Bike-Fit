lazy val root = (project in file("."))
  .enablePlugins(PlayScala, PlayEbean, PlayEnhancer, SbtWeb)
  .settings(
    name := "kings-bike-fit",
    organization := "uk.ac.kcl",
    version := "1.0-SNAPSHOT",
    scalaVersion := "2.13.1",
    libraryDependencies ++= Seq(
      guice,
      "org.scalatestplus.play" %% "scalatestplus-play" % "5.0.0" % Test,
      jdbc,
      evolutions,
      "mysql" % "mysql-connector-java" % "8.0.15",
      "com.h2database" % "h2" % "1.4.192",
      "org.mindrot" % "jbcrypt" % "0.4",
      "com.typesafe.akka" %% "akka-http-spray-json" % "10.1.11",
      "com.typesafe.akka" %% "akka-http-xml" % "10.1.11",
      "io.methvin.play" %% "autoconfig-macros" % "0.3.2"
    ),
    scalacOptions ++= Seq(
      "-feature",
      "-deprecation",
      "-Xfatal-warnings"
    ),
    JsEngineKeys.engineType := JsEngineKeys.EngineType.Node,

    // Exclude API docs from prod
    sources in (Compile, doc) := Seq.empty,
    publishArtifact in (Compile, packageDoc) := false
  )
