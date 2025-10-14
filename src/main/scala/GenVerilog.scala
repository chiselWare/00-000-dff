// (c) 2025 Rocksavage Technology, Inc.
// This code is licensed under the Apache Software License 2.0 (see LICENSE.MD)

package org.chiselware.dff

import chisel3._
import chisel3.util._
import _root_.circt.stage.ChiselStage

/** Generate Verilog for different configurations of the core all at once. This
  * is useful for generating different test cases for synthesis tools.
  */

object GenVerilog extends App {

  /** Create a map of Vectors to specify the different configurations to
    * generate in a comma-separated value format. The key is the name of the
    * configuration and the value is a Vector of parameters.
    */
  val config = Map(
    "small" -> Vector(1),
    "medium" -> Vector(32),
    "large" -> Vector(1024)
  )

  // Iterate through the configuration map and generate Verilog for each
  config.foreach { case (testName, paramVec) =>
    val thisWidth = paramVec(0).asInstanceOf[Int]
    val myParams = DffParams(
      width = thisWidth
    )

    println(
      s"Generating Verilog config: $testName\t" +
        s"width = $thisWidth"
    )

    // Generate basic Verilog (suppress SV features with lowering, etc)
    ChiselStage.emitSystemVerilog(
      new Dff(myParams),
      firtoolOpts = Array(
        "--lowering-options=disallowLocalVariables,disallowPackedArrays",
        "--disable-all-randomization",
        "--strip-debug-info",
        "--split-verilog",
        s"-o=generated/synTestCases/$testName"
      )
    )
  }
}
