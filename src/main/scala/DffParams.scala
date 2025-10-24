// (c) <year> <your name or company>
// This code is licensed under the <name of license> (see LICENSE.MD)

package org.chiselware.dff

import chisel3._
import chisel3.util._
import scala.collection.mutable.LinkedHashMap

/** Default parameter settings for Dff
  *
  * @constructor
  *   default parameter settings
  * @param width
  *   specifies the width of the Dff
  * @author
  *   Warren Savage
  * @version 1.0
  *
  * @see
  *   [[http://www.mycompany.com]] for more information
  */
case class DffParams(
    width: Int = 1
) {
  require(width >= 1, "Width must be greater than or equal 1")
}

/** Define a companion object to hold a Map of the configurations and the order
  * in which to be tested.
  *
  * Provide a meaningful configuration name for each set of parameter
  * configurations.
  * ```
  *  For sim, use lowerCamel case conventions: myConfig1, etc.
  *  For syn, use snake_case, typical for Verilog: my_config_1
  * ```
  */
object DffParams {
  val simConfigMap = LinkedHashMap[String, DffParams](
    "config8" -> DffParams(width = 8),
    "config32" -> DffParams(width = 32),
    "config64" -> DffParams(width = 64)
  )

  val synConfigMap = LinkedHashMap[String, DffParams](
    "config_1" -> DffParams(width = 1),
    "config_64" -> DffParams(width = 64),
    "config_128" -> DffParams(width = 128)
  )
}
