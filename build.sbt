name := "impact"

version := "0.1"

scalaVersion := "2.11.5"

// For running using `sbt run`
libraryDependencies ++= Seq(
  "org.jsoup" % "jsoup" % "1.8.1"
)

javaOptions += "-XstartOnFirstThread"
