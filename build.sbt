sbtPlugin := true

organization := "tv.cntt"

name := "xitrum-plugin"

version := "1.4"

// Kenji Yoshida (https://github.com/xuwei-k):
// scalaVersion should not be specified for SBT plugin; use default scalaVersion
//scalaVersion := "2.10.2"

scalacOptions ++= Seq(
  "-deprecation",
  "-feature",
  "-unchecked"
)

// http://www.scala-sbt.org/release/docs/Detailed-Topics/Java-Sources
// Avoid problem when Xitrum is built with Java 7 but the projects that use Xitrum
// are run with Java 6
// java.lang.UnsupportedClassVersionError: xitrum/annotation/First : Unsupported major.minor version 51.0
javacOptions ++= Seq("-source", "1.6", "-target", "1.6")
