// (c) <year> <your name or company>
// This code is licensed under the <name of license> (see LICENSE.MD)

package org.chiselware.dff.utils

import chisel3._
import chisel3.util._
import scala.collection.mutable.LinkedHashMap
import java.io.{File, PrintWriter}
import org.chiselware.dff._

/** Synthesis utilities
  *
  * These set of utilities automatically generate the synthesis regression
  * harness for all the specified configurations of the core.
  *
  * The three utilities are:
  * ```
  * 1. yosysTclFile generates a synthesis script for each configuration
  * 2. staTclFile generates a STA script for each configuration
  * 3. sdcFile generates a script for each configuration
  * 4. runScriptFile generates a bash script for running synthesis and STA
  *
  * Structure is as follows:
  *    generated/synTestCases/
  *      run.sh
  *      area_summary.rpt
  *      timing_summary.rpt
  *      <configName>/
  *        filelist.f
  *        <class>.sv
  *        <class>_net.v
  *        <class>.sdc
  *        <class>.yo.tcl
  *        <class>.sta.tcl
  *        yosys.log
  *        timing.rpt
  * ```
  */
// Generate the TCL synthesis script for Yosys
object yosysTclFile {
  def create(mainClassName: String, synTestDir: String): Unit = {
    val techLib = "../../../src/main/resources/stdcells.lib"
    val yosysFileData = s"""
      |yosys -import
      |
      |# Read in all the Verilog files in the filelist
      |set f [open filelist.f]
      |while {[gets $$f line] > -1} {
      |  read_verilog -sv $$line
      |}
      |close $$f
      |
      |# Synthesize, optimize, write out netlist and report
      |hierarchy -check -top $mainClassName
      |synth -top $mainClassName
      |flatten
      |dfflibmap -liberty $techLib
      |abc -liberty $techLib
      |opt_clean -purge
      |write_verilog -noattr ${mainClassName}_net.v
      |stat -liberty $techLib
    """.stripMargin.trim

    println(s"Writing Yosys TCL file to $synTestDir")
    val yosysFileDir = new File(synTestDir)
    yosysFileDir.mkdirs()
    val yosysFileName = new File(s"$yosysFileDir/$mainClassName.yo.tcl")
    val yosysFile = new PrintWriter(yosysFileName)
    yosysFile.write(s"${yosysFileData}")
    yosysFile.close()
  }
}

// Generate the TCL synthesis script for STA
object staTclFile {
  def create(mainClassName: String, synTestDir: String): Unit = {
    val techLib = "../../../src/main/resources/stdcells.lib"
    val staFileData = s"""
      |read_liberty $techLib
      |read_verilog ${mainClassName}_net.v
      |link_design $mainClassName
      |source $mainClassName.sdc
      |check_setup
      |report_checks
    """.stripMargin.trim

    println(s"Writing STA TCL file to $synTestDir")
    val staFileDir = new File(synTestDir)
    staFileDir.mkdirs()
    val staFileName = new File(s"$staFileDir/$mainClassName.sta.tcl")
    val staFile = new PrintWriter(staFileName)
    staFile.write(s"${staFileData}")
    staFile.close()
  }
}

// Generate the bash shell script for running synthesis and STA
object runScriptFile {
  def create(
      mainClassName: String,
      configs: String
  ): Unit = {
    val nand2Area = 0.798 // Nangate 45nm

    val runScriptFileData = s"""
      |#!/bin/bash
      |declare -a arr=(${configs})
      |
      |# Synthesize each of the test cases
      |for testCase in "$${arr[@]}"
      |do
      |  cd $$testCase
      |  echo "*** Synthesizing test case:  " $$testCase
      |  echo "" 
      |  yosys -Qv 1 -l yosys.log $mainClassName.yo.tcl  
      |  echo "" 
      |  
      |  echo "$mainClassName gate count report" 
      |  echo "---------------------------------------------------"
      |  file=yosys.log
      |  areaLine=$$(grep "Chip area" $$file)
      |  floatArea=$$(echo $$areaLine| cut -d':' -f 2)
      |  intArea=$$(echo $${floatArea%.*})
      |  gates=$$(echo "$$intArea/$nand2Area" | bc)
      |  echo -e "$$testCase = \t $$gates gates" >> ../area_summary.rpt
      |  echo -e "$$testCase = \t $$gates gates" 
      |  echo ""
      |
      |  echo "*** Running STA on " $$testCase
      |  sta -no_init -no_splash -exit $mainClassName.sta.tcl | tee ./timing.rpt
      |  timing=`grep slack ./timing.rpt`
      |  echo -e "$$testCase = \t $$timing" >> ../timing_summary.rpt 
      |  echo -e "$$testCase = \t $$timing"
      |  cd ..
      |done
    """.stripMargin.trim

    println(
      s"Writing script to run static timing analysis on this configuration of $mainClassName"
    )
    val runScriptFileName = new File(s"./generated/synTestCases/run.sh")
    val runScriptFile = new PrintWriter(runScriptFileName)
    runScriptFile.write(s"${runScriptFileData}")
    runScriptFile.close()
  }
}
