package com.pirum.exercises.worker

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import akka.actor.{ActorSystem, Props}
import akka.testkit.TestProbe
import org.scalatest.wordspec.AnyWordSpecLike

import java.util.concurrent.TimeUnit
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.FiniteDuration

class ProgramActorSpec extends ScalaTestWithActorTestKit with AnyWordSpecLike {
  implicit lazy val actorSystem: ActorSystem = ActorSystem("TestActor")

  "ProgramActor" should {
    "process a successful task" in {
      val probe = testKit.createTestProbe[Action]
      val parent = TestProbe()
      val child = parent.childActorOf(
        Props(
          new ProgramActor()
        )
      )
      child ! ProcessTask(
        SuccessfulTask("Task1", FiniteDuration(1, TimeUnit.SECONDS)),
        probe.ref
      )
      probe.expectMessage(Succeeded("Task1"))
    }

    "process a throwing task" in {
      val probe = testKit.createTestProbe[Action]
      val parent = TestProbe()
      val child = parent.childActorOf(
        Props(
          new ProgramActor()
        )
      )
      child ! ProcessTask(
        ThrowingTask("Task1", FiniteDuration(1, TimeUnit.SECONDS)),
        probe.ref
      )
      probe.expectMessage(Failed("Task1"))
    }

    "process a timeout task" in {
      val probe = testKit.createTestProbe[Action]
      val parent = TestProbe()
      val child = parent.childActorOf(
        Props(
          new ProgramActor()
        )
      )
      child ! ProcessTask(
        TimeoutTask("Task1", FiniteDuration(1, TimeUnit.SECONDS)),
        probe.ref
      )
      probe.expectMessage(TimedOut("Task1"))
    }
  }
}
