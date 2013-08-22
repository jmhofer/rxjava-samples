import sbt._
import Keys._

object Build extends Build {
  lazy val root = Project(
      id = "rxjava-samples", 
      base = file("."), 
      settings = Project.defaultSettings ++ rootSettings)

  lazy val rootSettings: Seq[Setting[_]] = Seq(

    name := "rxjava-samples",
    version := "1.0.0-SNAPSHOT",
    organization := "de.johoop",
    scalaVersion := "2.10.2",
    scalacOptions += "-language:_",

    resolvers += "Local Maven Repository" at Path.userHome.toURI + ".m2/repository",

    libraryDependencies ++= Seq(
      "org.scala-lang" % "scala-swing" % "2.10.2",
      "com.netflix.rxjava" % "rxjava" % "0.10.1-SNAPSHOT" changing, // jmhofer/RxJava, branch multi-static-final, published locally
      "com.netflix.rxjava" % "rxjava-scala" % "0.10.1-SNAPSHOT" changing() intransitive(),
      "com.netflix.rxjava" % "rxjava-swing" % "0.10.1-SNAPSHOT" changing() intransitive(),
      "org.slf4j" % "slf4j-simple" % "1.7.4"))
}
