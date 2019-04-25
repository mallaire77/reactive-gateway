package com.yoppworks.rxgateway.server

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import org.scalatest.WordSpecLike
import com.yoppworks.rxgateway.api._

/** Unit Tests For ReactiveGatewayGRPCTest */
class ReactiveGatewayGRPCTest extends ScalaTestWithActorTestKit with
  WordSpecLike {

  "ReactiveGatewayGRPCTest" should {
    "allow constructing requests with default values" in {
      val r1 = PrepareShapes()
      val r2 = GetAShape()
      val r3 = GetSomeShapes()
      val r4 = GetSomeTetrisShapes()
      val r5 = ReleaseShapes()
      assert(r1.numberOfShapesToPrepare == 0)
      assert(r2.index == 0)
      assert(r3.intervalMs == 0)
      assert(r4.dropSpots == Seq.empty[Int])
      assert(r5 != null)
      succeed
    }
  }
}
