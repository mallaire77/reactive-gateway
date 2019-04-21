package com.yoppworks.rxgateway.server

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import org.scalatest.WordSpecLike

/** Unit Tests For ConventionEnforcementSpec */
class EnforcedProtocolSpec extends ScalaTestWithActorTestKit with
  WordSpecLike {
  
  "EnforcedProtocolSpec" must {
    "allow normal getAShape transitions" in {
  
      val probe = createTestProbe[Either[String,EnforcedProtocol.State]]
      val actor = spawn(EnforcedProtocol("foo"))
  
      actor ! EnforcedProtocol.TransitionToPrepareShapes(probe.ref)
      probe.receiveMessage() match {
        case Right(_) ⇒
          actor ! EnforcedProtocol.TransitionToGetAShape(probe.ref)
          probe.receiveMessage match {
            case Right(_) ⇒
              actor ! EnforcedProtocol.TransitionToReleaseShapes(probe.ref)
              probe.receiveMessage match {
                case Right(_) ⇒
                  succeed
                case Left(message) ⇒
                  fail(message)
              }
            case Left (message) ⇒
              fail(message)
          }
        case Left(message) ⇒
          fail(message)
      }
    }
  }
}
