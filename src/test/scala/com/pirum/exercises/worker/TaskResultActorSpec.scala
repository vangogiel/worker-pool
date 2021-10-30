package com.pirum.exercises.worker

import akka.actor.ActorSystem
import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import com.pirum.exercises.worker.MonitoringActor.{
  CompleteResultSummaryNow,
  AttemptResultSummary
}
import com.pirum.exercises.worker.TaskResultActor.{
  CheckState,
  CheckStateAndFinalise,
  Failed,
  Succeeded,
  TimedOut
}
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import org.scalatest.wordspec.AnyWordSpecLike

class TaskResultActorSpec
    extends ScalaTestWithActorTestKit
    with AnyWordSpecLike {
  implicit lazy val actorSystem: ActorSystem = ActorSystem("TestActor")

  "TaskResultActor" should {
    "increment succeeded" in {
      val probe = testKit.createTestProbe[MonitoringActor.Command]()
      val subject = testKit.spawn(
        TaskResultActor.processCompletedTaskActor(
          SucceededTasks(List.empty),
          FailedTasks(List.empty),
          TimedOutTasks(List.empty)
        )
      )
      subject ! Succeeded("task1")
      subject ! CheckState(probe.ref)
      probe
        .expectMessageType[AttemptResultSummary]
        .succeeded
        .events
        .length mustBe 1
    }

    "increment failed" in {
      val probe = testKit.createTestProbe[MonitoringActor.Command]()
      val subject = testKit.spawn(
        TaskResultActor.processCompletedTaskActor(
          SucceededTasks(List.empty),
          FailedTasks(List.empty),
          TimedOutTasks(List.empty)
        )
      )
      subject ! Failed("task1")
      subject ! CheckState(probe.ref)
      probe
        .expectMessageType[AttemptResultSummary]
        .failed
        .events
        .length mustBe 1
    }

    "increment timedOut" in {
      val probe = testKit.createTestProbe[MonitoringActor.Command]()
      val subject = testKit.spawn(
        TaskResultActor.processCompletedTaskActor(
          SucceededTasks(List.empty),
          FailedTasks(List.empty),
          TimedOutTasks(List.empty)
        )
      )
      subject ! TimedOut("task1")
      subject ! CheckState(probe.ref)
      probe
        .expectMessageType[AttemptResultSummary]
        .timedOut
        .events
        .length mustBe 1
    }

    "check state" in {
      val probe = testKit.createTestProbe[MonitoringActor.Command]()
      val subject = testKit.spawn(
        TaskResultActor.processCompletedTaskActor(
          SucceededTasks(List.empty),
          FailedTasks(List.empty),
          TimedOutTasks(List.empty)
        )
      )
      subject ! CheckState(probe.ref)
      probe.expectMessageType[AttemptResultSummary]
    }

    "check state and finalise" in {
      val probe = testKit.createTestProbe[MonitoringActor.Command]()
      val subject = testKit.spawn(
        TaskResultActor.processCompletedTaskActor(
          SucceededTasks(List.empty),
          FailedTasks(List.empty),
          TimedOutTasks(List.empty)
        )
      )
      subject ! CheckStateAndFinalise(probe.ref)
      probe.expectMessageType[CompleteResultSummaryNow]
    }
  }
}
