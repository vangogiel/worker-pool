scalaVersion := "2.13.5"

organization := "com.pirum"
organizationName := "Pirum Systems"
organizationHomepage := Some(url("https://www.pirum.com"))

libraryDependencies := Seq(
  "com.typesafe.akka" %% "akka-actor-typed" % "2.6.17",
  "org.scalatest" %% "scalatest" % "3.1.4",
  "com.typesafe.akka" %% "akka-testkit" % "2.6.17"
)
