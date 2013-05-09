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
    scalaVersion := "2.10.1",
    
    resolvers += "Local Maven Repository" at "file://" + Path.userHome.absolutePath + "/.m2/repository",

    libraryDependencies ++= Seq(
      "org.scala-lang" % "scala-swing" % "2.10.1",
      "com.netflix.rxjava" % "rxjava-core" % "0.8.3" changing,  // currently requires a locally published version
      "com.netflix.rxjava" % "rxjava-swing" % "0.8.3" changing, // currently requires a locally published version 
      "org.slf4j" % "slf4j-simple" % "1.7.4"))
}
