// (c) 2025 Rocksavage Technology, Inc.
// This code is licensed under the Apache Software License 2.0 (see LICENSE.MD)

package org.chiselware.dff

import chisel3._
import chisel3.util._

object SimUtils {

  /** flatten recursively flattens a Chisel Data (Bundle, Vec, UInt, etc.) into
    * a sequence of (path, Bool) pairs representing every bit.
    *
    * @param d
    *   The Chisel `Data` object (e.g., Bundle, Vec, UInt, Bool) to recursively
    *   flatten.
    * @param prefix
    *   The hierarchical name prefix used to build full signal paths during
    *   recursion (e.g., "io.in[3]").
    * @return
    *   A sequence of (path, Bool) pairs representing every individual bit in
    *   `d` with its hierarchical name.
    */
  def flatten(d: Data, prefix: String = ""): Seq[(String, Bool)] =
    d match {
      case b: Bool =>
        Seq(prefix.stripPrefix(".") -> b)

      case bits: Bits =>
        val u = bits.asUInt
        (0 until u.getWidth).map(i => s"${prefix.stripPrefix(".")}[$i]" -> u(i))

      case r: Record =>
        // r.elements is a ListMap in declaration order
        r.elements.toSeq.flatMap { case (name, field) =>
          val next = if (prefix.isEmpty) name else s"$prefix.$name"
          flatten(field, next)
        }

      case v: Vec[_] =>
        v.zipWithIndex.flatMap { case (elem, i) =>
          val next = s"$prefix[$i]"
          flatten(elem, prefix = next)
        }
    }
}
