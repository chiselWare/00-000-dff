// (c) <year> <your name or company>
// This code is licensed under the <name of license> (see LICENSE.MD)

package org.chiselware.dff

import chisel3._
import chisel3.util._
import chiseltest._
import chiseltest.coverage._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.Assertions._
import firrtl2.options.TargetDirAnnotation
import scala.util.Random
import scala.math.pow
import java.io.{File, FileWriter, PrintWriter, BufferedWriter}
import org.chiselWare.dff.TestUtils.{checkCoverage}
//import org.chiselware.dff.Main.myParams
import scala.collection.mutable.LinkedHashMap

/** Highly randomized test suite driven by configuration parameters.
  *
  * Define a main test body that fully tests the core for one parameter set.
  *
  * Define sets of configuration parameters to be tested. Use a Map to hold the
  * configuration and parameter sets as a key-value pair.
  *
  * Iterate over the Map, executing the test for each configuration
  */
class DffTest extends AnyFlatSpec with Matchers with ChiselScalatestTester {

  // Parameter values to be tested and the order to execute them
  val configMap = LinkedHashMap[String, DffParams](
    "config1" -> DffParams(width = 1),
    "config64" -> DffParams(width = 64),
    "config128" -> DffParams(width = 128)
  )

  for ((configName, config) <- configMap) {
    main(configName, config)
  }

  // Create a directory for storing the Scala coverage reports
  val scalaCoverageDir = new File("generated/scalaCoverage")
  scalaCoverageDir.mkdir()

  behavior of "Configuration assertions"
  // Test that illegal combinations are caught
  it should "throw an assertion when dataWidth parameter is less than 1" in {
    val myParams = assertThrows[IllegalArgumentException] {
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
      // VerilatorBackendAnnotation,
      IcarusBackendAnnotation,
      // VcsBackendAnnotation,
      TargetDirAnnotation("generated")
    )

    it should "perform these tests successfully " in {
      val myParams = DffParams(width = p.width)
      val cov = test(new DffTb(myParams))
        .withAnnotations(backendAnnotations) { dut =>
          dut.clock.setTimeout(0)

          // Initialize the inputs
          dut.io.enable.poke(false.B)
          dut.io.in.poke(0.U)

          // Reset sequence
          dut.reset.poke(true.B)
          dut.clock.step()
          dut.reset.poke(false.B)
          info("Reset to zero")
          dut.io.out.expect(0.U)

          val randData = Random.nextInt(pow(2, p.width).toInt)
          dut.io.in.poke(randData)
          dut.io.enable.poke(1.U)
          dut.clock.step()
          info("Load data")
          dut.io.out.expect(randData)
          dut.io.enable.poke(0.U)
          dut.clock.step()
          info("Hold data")
          dut.io.out.expect(randData)
        }

      if (myParams.coverage) {
        val coverage = cov.getAnnotationSeq
          .collectFirst { case a: TestCoverage => a.counts }
          .get
          .toMap

        val testConfig = myParams.width.toString + "_"
        val verCoverageDir = new File("generated/verilogCoverage")
        verCoverageDir.mkdir()
        val coverageFile =
          verCoverageDir.toString + "/" + configName + "_" + testConfig + ".cov"

        val stuckAtFault = checkCoverage(coverage, coverageFile)
        if (stuckAtFault)
          fail(s"At least one IO port did not toggle -- see $coverageFile")
        info(s"Verilog Coverage report written to $coverageFile")
      }
    }
  }
}
