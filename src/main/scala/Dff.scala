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

  /** Pin-level coverage analysis. Each input and output pin is checked to see
    * if they toggle during simulation. This is like a pin-level code coverage.
    *
    * The tick value is used later to count the number clock ticks used to
    * calculate the coverage percentage.
    *
    * When coverage is enabled, the generated Verilog is annotated with coverage
    * points for all the ports in the IO Bundle.
    *
    * The code below does this automatically for every port in the IO Bundle.
    */

  if (p.coverage) {
    val tick = true.B
    val collectPorts: Seq[(String, Bool)] = SimUtils
      .flatten(io, "io_")
      .map { case (name, b) =>
        name -> (b.asUInt)(0)
      }
    cover(tick).suggestName("tick")
    collectPorts.foreach { case (name, bit) =>
      cover(bit).suggestName(s"$name")
    }
  }

}

/** Generate Verilog and its associated SDC file */
object Main extends App {
  val myParams = DffParams(width = 8, coverage = true)
  ChiselStage.emitSystemVerilog(
    new Dff(myParams),
    firtoolOpts = Array(
      "--lowering-options=disallowLocalVariables,disallowPackedArrays",
      "--disable-all-randomization",
      "--strip-debug-info",
      "--verilog",
      "--split-verilog",
      "-o=generated"
    )
  )
  GenSdcFile.run(myParams, "./generated/syn")
}
