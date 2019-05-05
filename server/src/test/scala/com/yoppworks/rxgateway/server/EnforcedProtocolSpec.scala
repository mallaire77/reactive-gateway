package com.yoppworks.rxgateway.server

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration._

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import com.yoppworks.rxgateway.server.EnforcedProtocol.{ToGetAShape, ToPrepareShapes}
import org.scalatest.{Assertion, WordSpecLike}

/** Unit Tests For ConventionEnforcementSpec */
class EnforcedProtocolSpec extends ScalaTestWithActorTestKit with
  WordSpecLike {
  
  val id = "test-id"
  
  "EnforcedProtocolSpec" must {
    "allow normal getAShape transitions" in {
      implicit val ec: ExecutionContext = ExecutionContext.global
  
      val f1 = EnforcedProtocol
        .tryTransition(id, ToPrepareShapes)
        .map {
          case None ⇒
            succeed
          case Some(message) ⇒
            fail(message)
        }.recover {
        case x: Exception ⇒
          fail(x.getClass.getName + ": " + x.getMessage)
      }
      val f2 = f1.flatMap[Assertion] { x ⇒
        if (x == succeed) {
          EnforcedProtocol.tryTransition(id, ToGetAShape).map {
            case None ⇒
              succeed
            case Some(message) ⇒
              fail(message)
          }
        } else { Future.successful ( x ) }
      }.recover {
        case x: Exception ⇒
          fail(x.getClass.getName + ": " + x.getMessage)
      }
      Await.result(f2, 10.seconds)
    }
    /*
    "prevent getSAShape to follow getSomeShapes" in {
      val probe = createTestProbe[ Either[ String, EnforcedProtocol.State ] ]
      val actor = spawn(EnforcedProtocol("foo"))
  
      actor ! EnforcedProtocol.ToPrepareShapes(probe.ref)
      probe.receiveMessage() match {
        case Right(_) ⇒
          actor ! EnforcedProtocol.ToGetSomeShapes(probe.ref)
          probe.receiveMessage match {
            case Right(_) ⇒
              actor ! EnforcedProtocol.TransitionToGetAShape(probe.ref)
              probe.receiveMessage match {
                case Right(_) ⇒
                  fail("Should have prevented transition to GetSomeShapes")
                case Left(_) ⇒
                  succeed
              }
            case Left(message) ⇒
              fail(message)
          }
        case Left(message) ⇒
          fail(message)
      }
    }
    
     */
  }
}
