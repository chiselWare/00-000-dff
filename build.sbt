//════ Core Configuration ══════════════════════════════════════════════════════
val coreNameLc = "dff"
val coreVersion = "1.0.0"
val orgId = "00"
val teamId = "000"
val chiselWareVersion = "0.9.1" // chiselWare platform version used (from Maven)
val spdxCopyrightText = Seq("<COPYRIGHT HOLDER>") // add as many as needed
//════ End Core Configuration -- do not modify below this line ═════════════════

val spdxLicense = "Apache-2.0"
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
  baseDirectory.value / ".scalafix-test.conf",
)

ThisBuild / scalafixOnCompile := false
ThisBuild / scalacOptions += "-Wunused:imports"

inThisBuild(
  List(
    scalaVersion := chiselWareScalaVersion,
    semanticdbEnabled := true,
    semanticdbVersion := scalafixSemanticdb.revision,
  ),
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
    "org.chiselware" %% "chiselware-ipf" % chiselWareVersion,
    "org.chiselware" %% "verilogheaders" % chiselWareVersion,
  ),
  scalacOptions ++= Seq(
    "-language:reflectiveCalls",
    "-deprecation",
    "-feature",
    "-Xcheckinit",
    "-Ymacro-annotations",
  ),
  addCompilerPlugin(
    "org.chipsalliance" % "chisel-plugin" % chiselVersion cross
      CrossVersion.full,
  ),
)

lazy val root = (project.in(file(".")))
  .aggregate(core)
  .settings(
    name := "chiselware",
    publish / skip := true, // root is an aggregator only
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
    Compile / doc / skip := false,
    Compile / doc := {
      val out = (Compile / doc).value
      val pkgDir =
        out / "org" / "chiselware" / "cores" / s"o$orgId" / s"t$teamId" /
          coreNameLc
      val src = baseDirectory.value / "docs" / "user-guide" / "images"
      if (src.exists)
        IO.copyDirectory(src, pkgDir)
      out
    },
    // Generate BuildInfo so these values are defined only once
    Compile / sourceGenerators += Def.task {
      // Turn a Scala string into a Scala string literal: abc -> "abc"
      def lit(s: String) =
        "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"") + "\""

      val holders = spdxCopyrightText.map(lit).mkString(", ")

      val f = (Compile / sourceManaged).value / "org" / "chiselware" /
        "BuildInfo.scala"
      IO.write(
        f,
        s"""package org.chiselware
       |object BuildInfo {
       |  val coreVersion       = ${lit(version.value)}
       |  val chiselWareVersion = ${lit(chiselWareVersion)}
       |  val spdxCopyrightText = Seq($holders)
       |  val spdxLicense       = ${lit(spdxLicense)}
       |}
       |""".stripMargin,
      )
      Seq(f)
    }.taskValue,
  )
  .settings(commonSettings)
