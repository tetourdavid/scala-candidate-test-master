name := "scala_candidate_test"

version := "0.1"

scalaVersion := "2.12.7"

scalacOptions += "-Ypartial-unification"

libraryDependencies ++= Seq(
  "io.spray" %%  "spray-json" % "1.3.5",
  "com.typesafe.akka" %% "akka-http"      % "10.1.5",
  "com.typesafe.akka" %% "akka-actor"     % "2.5.4",
  "com.typesafe.akka" %% "akka-stream"    % "2.5.4",
  "junit"             %  "junit"          % "4.12"    % "test",
  "org.scalatest"     %%  "scalatest"      % "3.0.3"   % "test",
  "com.typesafe.akka" %% "akka-http-testkit" % "10.1.5" % "test",
  "com.typesafe.akka" %% "akka-http-spray-json" % "10.1.5"
)

addCommandAlias("c", "compile")
addCommandAlias("s", "scalastyle")
addCommandAlias("tc", "test:compile")
addCommandAlias("ts", "test:scalastyle")
addCommandAlias("t", "test")
addCommandAlias("to", "testOnly")
