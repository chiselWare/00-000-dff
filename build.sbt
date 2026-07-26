// -- Core Configuration ------------------------------------------------------
val coreNameLc = "dff"
val coreVersion = "0.1.0"
val orgId = "00"
val teamId = "000"
val chiselWareVersion = "0.8.0" // chiselWare platform version used (Maven)
// -- End Core Configuration -- do not modify below this line ------------------

val chiselVersion = "5.3.0"
val chiselTestVer = "5.0.2"
val scalafmtVersion = "2.5.0"
val scalaTestVer = "3.2.18"
val chiselWareScalaVersion = "2.13.13"

ThisBuild / scalaVersion := chiselWareScalaVersion
ThisBuild / version := coreVersion
ThisBuild / organization := "org.chiselware"
ThisBuild / organizationName := "Chiselware"

// Scalafix settings, special configuration for test
ThisBuild / scalafixConfig := Some(baseDirectory.value / ".scalafix.conf")
ThisBuild / (Test / scalafixConfig) := Some(
  baseDirectory.value / ".scalafix-test.conf"
)

ThisBuild / scalafixOnCompile := false
ThisBuild / scalacOptions += "-Wunused:imports"
inThisBuild(
  List(
    scalaVersion := chiselWareScalaVersion,
    semanticdbEnabled := true,
    semanticdbVersion := scalafixSemanticdb.revision
  )
)
ThisBuild / scalafixDependencies +=
  "org.chiselware" %% "chiselware-scalafix-rules" % chiselWareVersion

Compile / doc / scalacOptions ++= Seq("-groups", "-implicits")

Test / parallelExecution := false

lazy val commonSettings = Seq(
  libraryDependencies ++= Seq(
    "org.chipsalliance" %% "chisel" % chiselVersion,
    "edu.berkeley.cs" %% "chiseltest" % chiselTestVer % Test,
    "org.scalatest" %% "scalatest" % scalaTestVer % Test,
    "org.chiselware" %% "chiselware-syn" % chiselWareVersion,
    "org.chiselware" %% "chiselware-ipf" % chiselWareVersion
  ),
  scalacOptions ++= Seq(
    "-language:reflectiveCalls",
    "-deprecation",
    "-feature",
    "-Xcheckinit",
    "-Ymacro-annotations"
  ),
  addCompilerPlugin(
    "org.chipsalliance" % "chisel-plugin" % chiselVersion cross
      CrossVersion.full
  )
)

lazy val root = (project in file("."))
  .aggregate(core)
  .settings(
    name := "chiselware",
    publish / skip := true // root is an aggregator only
  )

lazy val core = project
  .in(file(s"modules/$coreNameLc"))
  .settings(
    name := s"chiselware-core-$coreNameLc",
    coverageDataDir := target.value / "../generated/scalaCoverage",
    coverageFailOnMinimum := true,
    coverageMinimumStmtTotal := 90,
    coverageMinimumBranchTotal := 95,
    publish / skip := true, // no API for cores
    Compile / mainClass :=
      Some(s"org.chiselware.cores.o$orgId.t$teamId.$coreNameLc.Main"),
    Compile / doc / skip := false
  )
  .settings(commonSettings)
