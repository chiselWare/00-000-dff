// (c) <year> <your name or company>
// This code is licensed under the <name of license> (see LICENSE.MD)

package org.chiselware.dff

import chisel3._
import chisel3.util._
import java.io.{File, PrintWriter}
import _root_.circt.stage.ChiselStage

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

//Generate Verilog and SDC files for regression testing
object GenVer extends App {
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
