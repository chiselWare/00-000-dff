// (c) <year> <your name or company>
// This code is licensed under the <name of license> (see LICENSE.MD)

package org.chiselware.dff

import chisel3._
import chisel3.util._
//import java.io.{File, PrintWriter}
import _root_.circt.stage.ChiselStage
import org.chiselware.dff.utils._

/** A D-Flip-Flop with asynchronous reset
  *
  * @constructor
  *   create a new Dff
  * @param width
  *   defines the data width of the Dff
  * @author
  *   Warren Savage
  * @todo
  *
  * @see
  *   [[http://www.yourcompany.com]] for more information.
  *
  * <img src="doc/images/user-guide/dff-block-diagram.png" />
  */

class Dff(p: DffParams) extends Module {
  val io = IO(new Bundle {
    val d = Input(UInt(p.width.W))
    val q = Output(UInt(p.width.W))
    val enable = Input(Bool())
  })

  val q = RegInit(0.U(p.width.W))

  when(io.enable) {
    q := io.d
  }

  io.q := q
}

/** Generate Verilog and related collateral for each configuration to be used as
  * part of the regression framework.
  */
object Main extends App {
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

    // Generate synthesis files and scripts
    sdcFile.create(configParams, s"./generated/synTestCases/$configName")
    utils.yosysTclFile.create("Dff", s"./generated/synTestCases/$configName")
    utils.staTclFile.create("Dff", s"./generated/synTestCases/$configName")
    utils.runScriptFile.create(
      "Dff",
      DffParams.synConfigs
    )
  }
}
