// (c) 2025 Rocksavage Technology, Inc.
// This code is licensed under the Apache Software License 2.0 (see LICENSE.MD)

package org.chiselware.cores.o01.t001.dff

import chisel3._
import chisel3.util._
import _root_.circt.stage.ChiselStage
import org.chiselware.syn.{YosysTclFile, StaTclFile, RunScriptFile}

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
  val mainClassName = "Dff"
  val coreDir = s"modules/${mainClassName.toLowerCase()}"
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
        s"-o=${coreDir}/generated/synTestCases/$configName"
      )
    )

    // Generate synthesis files and scripts
    sdcFile.create(
      configParams,
      s"${coreDir}/generated/synTestCases/$configName"
    )
    YosysTclFile.create(
      mainClassName,
      s"${coreDir}/generated/synTestCases/$configName"
    )
    StaTclFile.create(
      mainClassName,
      s"${coreDir}/generated/synTestCases/$configName"
    )
    RunScriptFile.create(
      mainClassName,
      DffParams.synConfigs,
      s"${coreDir}/generated/synTestCases"
    )
  }
}
