package com.yoppworks.rxgateway.server

import scala.concurrent.{Await, ExecutionContext}
import scala.concurrent.duration._

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import com.yoppworks.rxgateway.server.EnforcedProtocol.{ToGetAShape, ToGetSomeShapes, ToPrepareShapes}
import org.scalatest.{WordSpecLike}

/** Unit Tests For ConventionEnforcementSpec */
class EnforcedProtocolSpec extends ScalaTestWithActorTestKit with WordSpecLike {
  
  val id = "test-id"
  implicit val ec: ExecutionContext = system.executionContext
  
  "EnforcedProtocolSpec" must {
    "allow normal getAShape transitions" in {
      val future = for {
        f1 ← EnforcedProtocol.tryTransition(id + 1, ToPrepareShapes)
        f2 ← EnforcedProtocol.tryTransition(id + 1, ToGetAShape)
      } yield {
        if (f1.isEmpty && f2.isEmpty) {
          succeed
        } else {
          fail(f1.getOrElse(f2.get))
        }
      }
      Await.result(future, 2.seconds)
    }
    "prevent getSAShape to follow getSomeShapes" in {
      val future = for {
        f1 <- EnforcedProtocol.tryTransition(id + 2, ToPrepareShapes)
        f2 ← EnforcedProtocol.tryTransition(id + 2, ToGetSomeShapes)
        f3 ← EnforcedProtocol.tryTransition(id + 2, ToGetAShape)
      } yield {
        if (f1.isEmpty && f2.isEmpty && f3.isDefined) {
          succeed
        } else {
          fail(f1.getOrElse(f2.getOrElse(f3.get)))
        }
      }
      Await.result(future, 2.seconds)
    }
  }
  
  override def afterAll(): Unit = testKit.shutdownTestKit()
  
}
