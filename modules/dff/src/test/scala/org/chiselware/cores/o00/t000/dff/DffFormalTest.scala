// (c) <year> <your name or company>
// This code is licensed under the <name of license> (see LICENSE.MD)

package org.chiselware.cores.o00.t000.dff

import chiseltest._
import chiseltest.formal._
import firrtl2.options.TargetDirAnnotation
import org.scalatest.Tag
import org.scalatest.flatspec.AnyFlatSpec

/** Tag to allow formal tests to be run separately from simulation tests.
  *
  * sbt "testOnly * -- -l FormalTest" (exclude formal)
  *
  * sbt "testOnly * -- -n FormalTest" (formal only)
  */
object FormalTest extends Tag("FormalTest")

/** Formal test spec for Dff.
  *
  * BoundedCheck(k): BMC for k cycles after reset. Increase for deeper cores.
  *
  * Runs across all simConfigMap entries — one property set, all configs
  */
class DffFormalTest extends AnyFlatSpec with ChiselScalatestTester with Formal {

  val bound = BoundedCheck(10)

  behavior of "Dff formal"

  for ((configName, config) <- DffParams.simConfigMap) {
    it should s"pass all formal properties ($configName)" taggedAs FormalTest in {
      verify(
        new DffFormalTb(config),
        Seq(
          bound,
          TargetDirAnnotation(s"modules/dff/generated/formal/$configName")
        )
      )
    }
  }
}
