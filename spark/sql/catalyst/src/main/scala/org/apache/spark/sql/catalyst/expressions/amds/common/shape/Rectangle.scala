/*
 *  Copyright 2023 by DIMS Project
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.spark.sql.catalyst.expressions.amds.common.shape

case class Rectangle(low: Point[Any], high: Point[Any]) extends Shape {
  require(low.coord.length == high.coord.length)
  require(low <= high, s"$low, $high")

  def this(lowArray: Array[Double], highArray: Array[Double]) {
    this(Point[Any](0, lowArray), Point[Any](0, highArray))
  }

  val centroid = Point[Any](0, low.coord.zip(high.coord).map(x => x._1 + x._2 / 2.0))

  def intersects(other: Shape): Boolean = {
    other match {
      case p: Point[Any] => contains(p)
      case mbr: Rectangle => intersects(mbr)
      case r: SimpleRange => r.intersects(this)
    }
  }

  def intersects(other: Rectangle): Boolean = {
    require(low.coord.length == other.low.coord.length)
    for (i <- low.coord.indices)
      if (low.coord(i) > other.high.coord(i) || high.coord(i) < other.low.coord(i)) {
        return false
      }
    true
  }

  def contains(p: Point[Any]): Boolean = {
    require(low.coord.length == p.coord.length)
    for (i <- p.coord.indices)
      if (low.coord(i) > p.coord(i) || high.coord(i) < p.coord(i)) {
        return false
      }
    true
  }

  def contains(rectangle: Rectangle): Boolean = {
    require(low.coord.length == rectangle.low.coord.length)
    for (i <- low.coord.indices) {
      if (low.coord(i) > rectangle.low.coord(i) || high.coord(i) < rectangle.high.coord(i)) {
        return false
      }
    }
    true
  }

  override def minDist(other: Any): Double = {
    other match {
      case p: Point[Any] => minDist(p)
      case mbr: Rectangle => minDist(mbr)
    }
  }

  def minDist(p: Point[Any]): Double = {
    require(low.coord.length == p.coord.length)
    var ans = 0.0
    for (i <- p.coord.indices) {
      if (p.coord(i) < low.coord(i)) {
        ans += (low.coord(i) - p.coord(i)) * (low.coord(i) - p.coord(i))
      } else if (p.coord(i) > high.coord(i)) {
        ans += (p.coord(i) - high.coord(i)) * (p.coord(i) - high.coord(i))
      }
    }
    Math.sqrt(ans)
  }

  def minDist(other: Rectangle): Double = {
    require(low.coord.length == other.low.coord.length)
    var ans = 0.0
    for (i <- low.coord.indices) {
      var x = 0.0
      if (other.high.coord(i) < low.coord(i)) {
        x = Math.abs(other.high.coord(i) - low.coord(i))
      } else if (high.coord(i) < other.low.coord(i)) {
        x = Math.abs(other.low.coord(i) - high.coord(i))
      }
      ans += x * x
    }
    Math.sqrt(ans)
  }

  override def approxMinDist(other: Shape): Double = {
    other match {
      case p: Point[Any] => approxMinDist(p)
    }
  }


  def approxMinDist(p: Point[Any]): Double = {
    var ans = 0.0
    for (i <- p.coord.indices) {
      if (p.coord(i) < low.coord(i)) {
        ans = math.max(ans, low.coord(i) - p.coord(i))
      } else if (p.coord(i) > high.coord(i)) {
        ans = math.max(ans, p.coord(i) - high.coord(i))
      }
    }

    ans
  }

  def approxMinDist(other: Rectangle): Double = {
    var ans = 0.0
    for (i <- low.coord.indices) {
      var x = 0.0
      if (other.high.coord(i) < low.coord(i)) {
        x = Math.abs(other.high.coord(i) - low.coord(i))
      } else if (high.coord(i) < other.low.coord(i)) {
        x = Math.abs(other.low.coord(i) - high.coord(i))
      }
      ans = math.max(ans, x)
    }

    ans
  }

  def MBRDistance: Double = low.minDist(high)

  //  def volume: Double = (high - low).coord.product

  override def toString: String = "(" + low.toString + "," + high.toString + ")"
}
