// (c) 2025 Rocksavage Technology, Inc.
// This code is licensed under the Apache Software License 2.0 (see LICENSE.MD)

package org.chiselware.dff

import java.io.{File, PrintWriter}

/** Generate a basic SDC file for use with post-synthesis static timing
  * analysis.
  *
  * The illustration below shows the basic elements captured in an SDC file.
  *
  * The following variables modified according to the specifics of the module
  * being synthesized:
  *
  * ```
  *   period: inverse of the expected operating frequency (in nanoseconds)
  *   dutyCycle: ratio of time when the clock is high and low (typ. 50%)
  *   inputDelay: time when the data is valid from the last rising clock edge
  *   outputDelay: time when the data is valid to the next rising clock edge
  *
  *   +-------+
  * a-|       |
  *   |  and  |--> c
  * b-|       |
  *   +-------+
  *
  *            |<--------->| Period
  *            |<--->|<--->| Duty Cycle
  *             _____       _____       _____
  * clock _____|     |_____|     |_____|     |_____
  *
  *            |<->|       |<->| Input Delay
  *                 _______________________________
  *     a _________|
  *                             ___________________
  *     b _____________________|
  *
  *                                |<->| Output Delay
  *                                ________________
  * c ____________________________|
  *
  * ```
  * @constructor
  *   GenSdcFile.run(p, path)
  * @param p
  *   Configuration parameters from the case class
  * @param sdcFilePath
  *   Path to where the .sdc file will be written
  */

object GenSdcFile {
  def run(p: DffParams, sdcFilePath: String): Unit = {
    // Default constraints, tighten or loosen as necessary
    val period = 5.000 // ns
    val dutyCycle = 0.50
    val inputDelayPct = 0.2
    val outputDelayPct = 0.2

    // Calculated constraints, override as needed in SdcFileData
    val inputDelay = period * inputDelayPct
    val outputDelay = period * outputDelayPct
    val fallingEdge = period * dutyCycle

    val sdcFileData = s"""
    create_clock -period $period -waveform {0 $fallingEdge} clock
    set_input_delay -clock clock $inputDelay {reset}
    set_input_delay -clock clock $inputDelay {io_d}
    set_input_delay -clock clock $inputDelay {io_enable}
    set_output_delay -clock clock $outputDelay {io_q}
  """.stripMargin

    println(s"Writing SDC file to $sdcFilePath")
    val sdcFileDir = new File(sdcFilePath)
    sdcFileDir.mkdirs()
    val sdcFileName = new File(s"$sdcFilePath/Dff.sdc")
    val sdcFile = new PrintWriter(sdcFileName)
    sdcFile.write(s"${sdcFileData}")
    sdcFile.close()
  }
}
