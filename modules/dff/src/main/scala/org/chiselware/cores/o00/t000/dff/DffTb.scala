// (c) <year> <your name or company>
// This code is licensed under the <name of license> (see LICENSE.MD)

package org.chiselware.cores.o00.t000.dff

import chisel3._

/** DffTb is a simple test bench wrapper to hold a single instance of Dff.
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
