import AssemblyKeys._ // put this at the top of the file

assemblySettings

name := "ZhenhaiLifeTest"

scalaVersion := "2.11.7"

organization := "tw.com.zhenhai"

fork in run := true

scalacOptions += "-deprecation"

libraryDependencies ++= Seq(
  "commons-daemon" % "commons-daemon" % "1.0.15",
  "ch.qos.logback" % "logback-classic" % "1.1.2",
  "org.jfree" % "jfreechart" % "1.0.19",
  "jfree" % "jfreechart-swt" % "1.0.17",
  "org.xerial" % "sqlite-jdbc" % "3.8.11.2",
  "org.scream3r" % "jssc" % "2.8.0"
)
