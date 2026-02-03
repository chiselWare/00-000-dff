// (c) <year> <your name or company>
// This code is licensed under the <name of license> (see LICENSE.MD)

package org.chiselware.cores.o01.t001.dff

import _root_.circt.stage.ChiselStage
import chisel3._
import org.chiselware.syn.RunScriptFile
import org.chiselware.syn.StaTclFile
import org.chiselware.syn.YosysTclFile

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
  val MainClassName = "Dff"
  val coreDir = s"modules/${MainClassName.toLowerCase()}"

  DffParams.synConfigMap.foreach { case (configName, configParams) =>
    val myOpts = Array(
      "--lowering-options=disallowLocalVariables,disallowPackedArrays",
      "--disable-all-randomization",
      "--strip-debug-info",
      "--split-verilog",
      s"-o=${coreDir}/generated/synTestCases/$configName"
    )
    println()
    println(s"Generating Verilog for config: $configName")
    ChiselStage.emitSystemVerilog(
      gen = new Dff(p = configParams),
      firtoolOpts = myOpts
    )

    // Generate synthesis files and scripts
    SdcFile.create(
      p = configParams,
      sdcFilePath = s"${coreDir}/generated/synTestCases/$configName"
    )
    YosysTclFile.create(
      mainClassName = MainClassName,
      synTestDir = s"${coreDir}/generated/synTestCases/$configName"
    )
    StaTclFile.create(
      mainClassName = MainClassName,
      synTestDir = s"${coreDir}/generated/synTestCases/$configName"
    )
    RunScriptFile.create(
      mainClassName = MainClassName,
      configs = DffParams.synConfigs,
      runDir = s"${coreDir}/generated/synTestCases"
    )
  }
}
