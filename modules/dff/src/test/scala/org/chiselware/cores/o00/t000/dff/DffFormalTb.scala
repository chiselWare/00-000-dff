// (c) <year> <your name or company>
// This code is licensed under the <name of license> (see LICENSE.MD)

package org.chiselware.cores.o00.t000.dff

import chisel3._
import chiseltest.formal._

/** Formal testbench for Dff.
  *
  * chiselWare formal testbench pattern: properties live here, not in production
  * RTL. Analogous to DffTb for simulation. See the chiselWare Developer's Guide
  * for a full explanation of the methodology.
  *
  * Temporal primitives available via `import chiseltest.formal._`: past(sig) —
  * value of sig one cycle ago (SVA: $past) rose(sig) — true when sig
  * transitions 0->1 (SVA: $rose) fell(sig) — true when sig transitions 1->0
  * (SVA: $fell) stable(sig) — true when sig === past(sig) (SVA: $stable)
  *
  * Note: reset is chisel3.Reset, not Bool — always cast with .asBool. Note: for
  * registered outputs, use past(rose/fell(...)) not rose/fell(...) directly,
  * since the effect is visible one cycle after the edge. Note:
  * assert/assume/cover are Scala macros and do not support named parameters —
  * CWS named parameter rule does not apply to these three.
  *
  * @param p
  *   DffParams — same parameterization as the DUT
  */
class DffFormalTb(p: DffParams) extends Module {

  /** Wrapper IO — formal tool drives these as free variables. Required: FIRRTL
    * rejects undriven (VOID) DUT inputs.
    */
  val io = IO(new Bundle {
    val d = Input(UInt(p.width.W))
    val enable = Input(Bool())
  })

  val dut = Module(new Dff(p))
  dut.io.d := io.d
  dut.io.enable := io.enable

  // ---- assumptions ------------------------------------------------------
  /** None — all input combinations are legal for a DFF. Add assume() here to
    * constrain the environment for more complex cores.
    */

  // ---- assertions -------------------------------------------------------

  // 1. past() — output is zero the cycle after reset
  when(past(reset.asBool)) {
    assert(dut.io.q === 0.U, "output must be zero after reset")
  }

  // 2. past() — output captures input the cycle after enable is high
  when(past(io.enable) && !past(reset.asBool)) {
    assert(
      dut.io.q === past(io.d),
      "output must capture input when enable is high"
    )
  }

  // 3. past() — output holds when enable is low
  when(!past(io.enable) && !past(reset.asBool)) {
    assert(dut.io.q === past(dut.io.q), "output must hold when enable is low")
  }

  // 4. stable() — alternative expression of hold; convenient for Bundle outputs.
  //    Redundant with property 3 for a DFF — included to show the pattern.
  //    stable() is most valuable when the signal is a multi-field Bundle,
  //    where it replaces a field-by-field past() comparison.
  when(!past(io.enable) && !past(reset.asBool)) {
    assert(
      stable(dut.io.q),
      "output must be stable when enable is low (stable)"
    )
  }

  // 5. past(rose()) — output updates the cycle after enable rises.
  //    Use past(rose/fell(...)) for registered outputs — the effect is
  //    visible one cycle after the edge, not on the same cycle.
  when(past(rose(io.enable)) && !past(reset.asBool)) {
    assert(
      dut.io.q === past(io.d),
      "output must update the cycle after enable rises"
    )
  }

  // 6. past(fell()) — output holds the cycle after enable falls
  when(past(fell(io.enable)) && !past(reset.asBool)) {
    assert(
      dut.io.q === past(dut.io.q),
      "output must hold the cycle after enable falls"
    )
  }

  // 7. Invariant — no temporal operator; must hold every cycle
  val maxVal = ((BigInt(1) << p.width) - 1).U(p.width.W)
  assert(dut.io.q <= maxVal, "output must never exceed maximum value for width")

  // ---- cover ------------------------------------------------------------
  /** Reachability checks — if any fail, assumptions are over-constraining. */

  cover(reset.asBool, "reset is reachable")
  cover(io.enable, "enable high is reachable")
  cover(!io.enable, "enable low is reachable")
  cover(rose(io.enable), "rising edge of enable is reachable")
  cover(fell(io.enable), "falling edge of enable is reachable")
  cover(dut.io.q === maxVal, "output reaches maximum value")
  cover(dut.io.q === 0.U, "output reaches zero")
}
