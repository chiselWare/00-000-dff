// (c) <year> <your name or company>
// This code is licensed under the <name of license> (see LICENSE.MD)

package org.chiselware.cores.o00.t000.dff

import _root_.circt.stage.ChiselStage
import chisel3._
import org.chiselware.ipf.{ IpfJsonFile, ParamCli }
import org.chiselware.syn.{ RunScriptFile, StaTclFile, YosysTclFile }

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

/** Companion object to allow factory method instantiation, such as myDff =
  * Dff(width = 8)
  */

object Dff {
  def apply(params: DffParams): Dff = Module(new Dff(params))
}

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
  val coreDir = s"modules/${DffParams.MainClassName.toLowerCase()}"

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
      mainClassName = DffParams.MainClassName,
      synTestDir = s"${coreDir}/generated/synTestCases/$configName"
    )
    StaTclFile.create(
      mainClassName = DffParams.MainClassName,
      synTestDir = s"${coreDir}/generated/synTestCases/$configName"
    )
    RunScriptFile.create(
      mainClassName = DffParams.MainClassName,
      configs = DffParams.synConfigs,
      runDir = s"${coreDir}/generated/synTestCases"
    )
  }
}

/** Generate artifacts for the IP Factory to download upon a user request.
  * ```
  * This includes
  * - A single Verilog configuration consisting of a filelist.f & Verilog files
  * - An sdc file
  * - The core's User Guide (in PDF)
  * - A JSON file
  *
  * This is executed by the following SBT command line:
  *
  * sbt "project core" "runMain org.chiselware.cores.o00.t000.dff.GenVerWithParamCli -- --params='(width=8)'"
  * ```
  */
object GenVerWithParamCli extends App {
  val p = ParamCli.parseParams(args)
  val params = DffParams.fromMap(p)

  ChiselStage.emitSystemVerilog(
    new Dff(params),
    firtoolOpts = Array(
      "--lowering-options=disallowLocalVariables,disallowPackedArrays",
      "--disable-all-randomization",
      "--strip-debug-info",
      "--split-verilog",
      s"-o=.ipf"
    )
  )

  SdcFile.create(
    p = params,
    sdcFilePath = ".ipf"
  )

  IpfJsonFile.create(
    p = IpfParamsMap.params,
    jsonFilePath = s".ipf/${DffParams.MainClassName}.json"
  )

}
