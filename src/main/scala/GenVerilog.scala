// (c) <year> <your name or company>
// This code is licensed under the <name of license> (see LICENSE.MD)

package org.chiselware.dff

import chisel3._
import chisel3.util._
import _root_.circt.stage.ChiselStage

/** Generate different Verilog cores for generating synthesis regression tests.
  */

object GenVerilog extends App {

  DffParams.synConfigMap.foreach { case (configName, configParams) =>
    println()
    println(s"Generating Verilog for config: $configName")
    ChiselStage.emitSystemVerilog(
      new Dff(configParams),
      firtoolOpts = Array(
        "--lowering-options=disallowLocalVariables,disallowPackedArrays",
        "--disable-all-randomization",
        "--strip-debug-info",
        "--split-verilog",
        s"-o=generated/synTestCases/$configName"
      )
    )
    GenSdcFile.run(configParams, s"./generated/synTestCases/$configName")
  }
}
