// (c) <year> <your name or company>
// This code is licensed under the <name of license> (see LICENSE.MD)

package org.chiselware.dff

import chisel3._
import chisel3.util._

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
  *   [[http://www.<your company>]] for more information
  */
case class DffParams(
    width: Int = 1,
    coverage: Boolean = false
) {

  require(width >= 1, "Width must be greater than or equal 1")
}
