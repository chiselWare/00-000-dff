// (c) <year> <your name or company>
// This code is licensed under the <name of license> (see LICENSE.MD)

package org.chiselware.dff

import chisel3._
import chisel3.util._
import _root_.circt.stage.ChiselStage

/** DffTB is a simple test bench wrapper to hold a single instance of Dff.
  *
  * @param p
  *   A customization of default parameters contained in DffParams case class.
  */
class DffTb(p: DffParams) extends Module {
  val io = IO(new Bundle {
    val in = Input(UInt(p.width.W))
    val out = Output(UInt(p.width.W))
    val enable = Input(Bool())
  })

  val dut = Module(new Dff(p))
  dut.io.d := io.in
  dut.io.enable := io.enable
  io.out := dut.io.q
}
