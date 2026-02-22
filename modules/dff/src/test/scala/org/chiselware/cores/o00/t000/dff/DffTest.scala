// (c) <year> <your name or company>
// This code is licensed under the <name of license> (see LICENSE.MD)

package org.chiselware.cores.o00.t000.dff

import chisel3._
import chiseltest._
import firrtl2.options.TargetDirAnnotation
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import java.io.File

class DffTest extends AnyFlatSpec with Matchers with ChiselScalatestTester {

  // Execute the main test for each configuration
  for ((configName, config) <- DffParams.simConfigMap) {
    main(configName = configName, p = config)
  }

  val scalaCoverageDir = new File("generated/scalaCoverage")
  scalaCoverageDir.mkdir()

  // Test that illegal configuration combinations are caught
  behavior of "Configuration assertions"
  it should "throw an assertion when dataWidth parameter is less than 1" in {
    val widthErr = assertThrows[IllegalArgumentException] {
      DffParams(
        width = 0
      )
    }
  }

  /** Main test function executes one complete test for one configuration */
  def main(configName: String, p: DffParams): Unit = {

    behavior of s"Dff directed tests (config: $configName)"

    val backendAnnotations = Seq(
      // WriteVcdAnnotation,
      // WriteFstAnnotation,
      VerilatorBackendAnnotation,
      // IcarusBackendAnnotation,
      // VcsBackendAnnotation,
      TargetDirAnnotation("modules/dff/generated")
    )

    it should "perform these tests successfully " in {
      test(new DffTb(p))
        .withAnnotations(annotationSeq = backendAnnotations) { dut =>
          dut.clock.setTimeout(0)
          // Initialize the inputs
          dut.io.enable.poke(false.B)
          dut.io.in.poke(0.U)

          // Sequence: reset, loop: load, hold
          info("Reset to zero")
          dut.reset.poke(true.B)
          dut.clock.step()
          dut.reset.poke(false.B)
          dut.io.out.expect(0.U)
          info("Test with random data")
          for (i <- 1 to 10) {
            val myData = BigInt(p.width, scala.util.Random)
            dut.io.enable.poke(1.U)
            dut.io.in.poke(myData)
            dut.clock.step()
            dut.io.out.expect(myData)
            dut.io.enable.poke(0.U)
            dut.clock.step()
            dut.io.out.expect(myData)
          }
        }
    }
  }
}
