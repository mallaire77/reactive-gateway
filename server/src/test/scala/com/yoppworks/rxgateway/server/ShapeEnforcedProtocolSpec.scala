package com.yoppworks.rxgateway.server

import scala.concurrent.{Await, ExecutionContext}
import scala.concurrent.duration._

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import com.yoppworks.rxgateway.server.ShapeEnforcedProtocol.{ToGetAShape, ToGetSomeShapes, ToPrepareShapes, ToReleaseShapes}
import org.scalatest.WordSpecLike

/** Unit Tests For EnforcedProtocolSpec */
class ShapeEnforcedProtocolSpec extends ScalaTestWithActorTestKit with WordSpecLike {
  
  val id = "test-id"
  implicit val ec: ExecutionContext = system.executionContext
  
  "EnforcedProtocolSpec" must {
    "allow normal getAShape transitions" in {
      val myId = id + 1
      val future = for {
        f1 ← ShapeEnforcedProtocol.tryTransition(myId, ToPrepareShapes)
        f2 ← ShapeEnforcedProtocol.tryTransition(myId, ToGetAShape)
      } yield {
        if (f1.isEmpty && f2.isEmpty) {
          succeed
        } else {
          fail(f1.getOrElse(f2.get))
        }
      }
      Await.result(future, 2.seconds)
    }
    "prevent getSAShape following after getSomeShapes" in {
      val myId = id + 2
      val future = for {
        f1 ← ShapeEnforcedProtocol.tryTransition(myId, ToPrepareShapes)
        f2 ← ShapeEnforcedProtocol.tryTransition(myId, ToGetSomeShapes)
        f3 ← ShapeEnforcedProtocol.tryTransition(myId, ToGetAShape)
      } yield {
        if (f1.isEmpty && f2.isEmpty && f3.isDefined) {
          succeed
        } else {
          fail(f1.getOrElse(f2.getOrElse(f3.get)))
        }
      }
      Await.result(future, 2.seconds)
    }
  
    "ensure a complete transaction is permitted" in {
      val myId = id + 3
      val future = for {
        f1 ← ShapeEnforcedProtocol.tryTransition(myId, ToPrepareShapes)
        f2 ← ShapeEnforcedProtocol.tryTransition(myId, ToGetAShape)
        f3 ← ShapeEnforcedProtocol.tryTransition(myId, ToGetSomeShapes)
        f4 ← ShapeEnforcedProtocol.tryTransition(myId, ToGetSomeShapes)
        f5 ← ShapeEnforcedProtocol.tryTransition(myId, ToReleaseShapes)
      } yield {
        assert(Seq(f1,f2,f3,f4,f5).filterNot(_.isEmpty).isEmpty)
      }
      Await.result(future, 2.seconds)
    }
  }
}
