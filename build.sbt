name := "ZhenhaiLifeTest"

organization := "tw.com.zhenhai"

fork in run := true

libraryDependencies ++= Seq(
  "org.jfree" % "jfreechart" % "1.0.19",
  "jfree" % "jfreechart-swt" % "1.0.17",
  "org.xerial" % "sqlite-jdbc" % "3.8.11.2"
)
