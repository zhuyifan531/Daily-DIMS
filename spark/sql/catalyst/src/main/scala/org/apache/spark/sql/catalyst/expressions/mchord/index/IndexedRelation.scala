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
package org.apache.spark.sql.catalyst.expressions.mchord.index

import org.apache.spark.rdd.RDD
import org.apache.spark.sql.catalyst.expressions.Attribute
import org.apache.spark.sql.catalyst.expressions.mchord.PackedPartition
import org.apache.spark.sql.catalyst.plans.logical.LogicalPlan

abstract class IndexedRelation extends LogicalPlan {
  def indexedRDD: RDD[PackedPartition]
  def globalIndex: GlobalIndex

  override def output: Seq[Attribute] = Nil

  override def children: Seq[LogicalPlan] = Nil
}
