// SPDX-FileCopyrightText: <COPYRIGHT HOLDER>
// SPDX-License-Identifier: Apache-2.0

package org.chiselware.cores.o00.t000.dff

import chisel3._

import _root_.circt.stage.ChiselStage
import org.chiselware.BuildInfo.{
  chiselWareVersion, coreVersion, spdxCopyrightText, spdxLicense,
}
import org.chiselware.ipf.{ IpfJsonFile, ParamCli }
import org.chiselware.syn.{ RunScriptFile, StaTclFile, YosysTclFile }
import org.chiselware.verilogheaders.VerilogHeader

object Dff {

  def apply(params: DffParams): Dff = Module(new Dff(params))

}

/** A D-Flip-Flop with asynchronous reset
  *
  * <img src="Dff-blockdiagram.png" />
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

  val coreDir = s"modules/${DffParams.MainClassName.toLowerCase()}"

  DffParams.synConfigMap.foreach { case (configName, configParams) =>
    println()
    println(s"Generating Verilog for config: $configName")
    val outDir = s"$coreDir/generated/synTestCases/$configName"
    ChiselStage.emitSystemVerilogFile(
      gen = new Dff(p = configParams),
      args = Array("--target-dir", outDir, "--split-verilog"),
      firtoolOpts = Array(
        "--lowering-options=disallowLocalVariables,disallowPackedArrays",
        "--disable-all-randomization",
        "--strip-debug-info",
      ),
    )

    val prov = VerilogHeader.fromGit(
      coreVersion = coreVersion,
      cwVersion = chiselWareVersion,
      copyrightText = spdxCopyrightText,
      license = spdxLicense,
    )

    println(
      s"Stamping generated code for config $configName with provenance data",
    )
    VerilogHeader.stamp(dir = java.nio.file.Paths.get(outDir), p = prov)

    // Generate synthesis files and scripts
    SdcFile.create(
      p = configParams,
      sdcFilePath = s"${coreDir}/generated/synTestCases/$configName",
    )

    YosysTclFile.create(
      mainClassName = DffParams.MainClassName,
      synTestDir = s"${coreDir}/generated/synTestCases/$configName",
    )

    StaTclFile.create(
      mainClassName = DffParams.MainClassName,
      synTestDir = s"${coreDir}/generated/synTestCases/$configName",
    )

    RunScriptFile.create(
      mainClassName = DffParams.MainClassName,
      configs = DffParams.synConfigs,
      runDir = s"${coreDir}/generated/synTestCases",
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
  val OutDir = ".ipf"

  ChiselStage.emitSystemVerilogFile(
    new Dff(params),
    args = Array("--target-dir", OutDir, "--split-verilog"),
    firtoolOpts = Array(
      "--lowering-options=disallowLocalVariables,disallowPackedArrays",
      "--disable-all-randomization",
      "--strip-debug-info",
      s"-o=$OutDir",
    ),
  )

  val prov = VerilogHeader.fromGit(
    coreVersion = coreVersion,
    cwVersion = chiselWareVersion,
    copyrightText = spdxCopyrightText,
    license = spdxLicense,
  )

  println(s"Stamping generated code with provenance data")
  VerilogHeader.stamp(dir = java.nio.file.Paths.get(OutDir), p = prov)

  SdcFile.create(
    p = params,
    sdcFilePath = ".ipf",
  )

  IpfJsonFile.create(
    p = IpfParamsMap.params,
    jsonFilePath = s".ipf/${DffParams.MainClassName}.json",
  )

}
