// (c) <year> <your name or company>
// This code is licensed under the <name of license> (see LICENSE.MD)

package org.chiselware.cores.o00.t000.dff

import org.chiselware.ipf.{ FieldType, IpfParams }

import java.io.{ File, PrintWriter }
import scala.collection.mutable.LinkedHashMap

/** Default parameter settings for Dff
  *
  * @constructor
  *   default parameter settings
  * @param width
  *   specifies the width of the Dff
  * @author
  *   Warren Savage
  * @see
  *   [[http://www.mycompany.com]] for more information
  */
case class DffParams(
    width: Int = 1) {
  val widthMsg = "width must be greater than or equal 1"
  require(width >= 1, widthMsg)
}

/** Define a companion object to hold a Map of the configurations and the order
  * in which to be tested.
  *
  * Provide a meaningful configuration name for each set of parameter
  * configurations.
  * ```
  *  For sim, use lowerCamel case conventions: myConfig1, etc.
  *  For syn, use snake_case, typical for Verilog: my_config_1
  *
  *  Note that these are constants and thus follow the UpperCamelCase
  *  convention.
  * ```
  */
object DffParams {
  val MainClassName = "Dff"
  val simConfigMap = LinkedHashMap[String, DffParams](
    "config8" -> DffParams(width = 8),
    "config32" -> DffParams(width = 32),
    "config64" -> DffParams(width = 64)
  )

  val synConfigMap = LinkedHashMap[String, DffParams](
    "small_1" -> DffParams(width = 1),
    "medium_64" -> DffParams(width = 64),
    "large_128" -> DffParams(width = 128)
  )

  // Extract config names into a space-separated string
  val synConfigs = DffParams.synConfigMap
    .map { case (configName, config) => s"$configName" }
    .mkString(" ")

  // Package parameter names for import to IP Factory
  def fromMap(m: Map[String, String]): DffParams = {
    val width = m.get("width").map(_.toInt).getOrElse(1)
    // add all additional synthesis parameters here
    DffParams(width = width) // extend with more params as needed
  }
}

/** Customize this companion object with your port list and desired synthesis
  * contraints.
  */
object SdcFile {
  def create(
      p: DffParams,
      sdcFilePath: String
    ): Unit = {
    // Default constraints, tighten or loosen as necessary
    val period = 5.000 // ns
    val dutyCycle = 0.50
    val inputDelayPct = 0.2
    val outputDelayPct = 0.2

    // Calculated constraints, customize as needed in SdcFileData
    val inputDelay = period * inputDelayPct
    val outputDelay = period * outputDelayPct
    val fallingEdge = period * dutyCycle

    val sdcFileData =
      s"""
      |create_clock -period $period -waveform {0 $fallingEdge} clock
      |set_input_delay -clock clock $inputDelay {reset}
      |set_input_delay -clock clock $inputDelay {io_d}
      |set_input_delay -clock clock $inputDelay {io_enable}
      |set_output_delay -clock clock $outputDelay {io_q}
    """.stripMargin.trim

    println(s"Writing SDC file to $sdcFilePath")
    val sdcFileDir = new File(sdcFilePath)
    sdcFileDir.mkdirs()
    val sdcFileName = new File(s"$sdcFilePath/Dff.sdc")
    val sdcFile = new PrintWriter(sdcFileName)
    sdcFile.write(s"${sdcFileData}")
    sdcFile.close()
  }

}

object IpfParamsMap {
  val params = Map(
    "width" ->
      IpfParams(
        fieldType = FieldType.Field,
        pickList = List.empty,
        message = Some(DffParams().widthMsg)
        // add remaining parameters as needed, here are some examples
        // "useFifo" -> IpfParams(FieldType.Toggle, List.empty, None),
        // "cacheSize" -> IpfParams(FieldType.Pick, List("32", "64", "128"), None)
      )
  )
}
