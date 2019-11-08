name := "NatAPI"

version := "0.1"

scalaVersion := "2.11.12"

lazy val finchVersion = "0.16.0-M1"
lazy val circeVersion = "0.8.0"
lazy val finagleVersion = "6.45.0"
lazy val twitterServerVersion = "1.30.0"
lazy val slickVersion = "3.3.2"

lazy val typesafeConfig = "1.3.1"

lazy val scalaTestVersion = "3.0.1"

mainClass in (Compile, run) := Some("com.danielsan.natapi.Main")

libraryDependencies ++= Seq(
  "org.scala-lang.modules" %% "scala-xml" % "1.2.0",
  "com.github.finagle" %% "finch-core" % finchVersion,
  "com.github.finagle" %% "finch-circe" % finchVersion,
  "io.circe" %% "circe-generic" % circeVersion,
  "io.circe" %% "circe-parser" % circeVersion,
  "com.twitter" %% "twitter-server" % twitterServerVersion,
  "com.twitter" %% "finagle-stats" % finagleVersion,
  "com.typesafe.slick" %% "slick" % slickVersion,
  "com.typesafe.slick" %% "slick-hikaricp" % slickVersion,
  "mysql" % "mysql-connector-java" % "8.0.18",
  "com.typesafe" % "config" % typesafeConfig,
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "com.cloudinary" %% "cloudinary-core-scala" % "1.2.1",
  "org.scalatest" %% "scalatest" % scalaTestVersion % Test,
  "org.scalamock" %% "scalamock" % "4.4.0" % Test,
  "com.h2database" % "h2" % "1.4.200" % Test
)

enablePlugins(JavaAppPackaging)

fork in Test := true
javaOptions in Test += s"-Dconfig.file=${baseDirectory.value}/conf/test.conf"

javaOptions in run += s"-Dconfig.file=${baseDirectory.value}/conf/local.conf"
