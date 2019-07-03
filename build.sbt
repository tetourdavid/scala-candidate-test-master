name := "scala_candidate_test"

version := "0.1"

scalaVersion := "2.12.7"

scalacOptions += "-Ypartial-unification"

libraryDependencies ++= Seq(
  "io.spray" %%  "spray-json" % "1.3.5",
  "com.typesafe.akka" %% "akka-http"      % "10.1.5",
  "com.typesafe.akka" %% "akka-actor"     % "2.5.4",
  "com.typesafe.akka" %% "akka-stream"    % "2.5.4",
  "org.scalamock" %% "scalamock-scalatest-support" % "3.6.0" % Test,
  "junit"             %  "junit"          % "4.12"    % Test,
  "com.typesafe.akka" %% "akka-http-testkit" % "10.1.5" % Test,
  "com.typesafe.akka" %% "akka-http-spray-json" % "10.1.5",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2",
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "com.typesafe" % "config" % "1.3.4"
)

addCommandAlias("c", "compile")
addCommandAlias("s", "scalastyle")
addCommandAlias("tc", "test:compile")
addCommandAlias("ts", "test:scalastyle")
addCommandAlias("t", "test")
addCommandAlias("to", "testOnly")
