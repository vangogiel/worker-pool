package com.pirum.exercises.worker.actors

import akka.actor.ActorSystem
import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import com.pirum.exercises.worker.actors.MonitoringActor.{
  AttemptResultSummary,
  CompleteResultSummaryNow
}
import com.pirum.exercises.worker.actors.TasksStateActor._
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import org.scalatest.wordspec.AnyWordSpecLike

class TasksStateActorSpec
    extends ScalaTestWithActorTestKit
    with AnyWordSpecLike {
  implicit lazy val actorSystem: ActorSystem = ActorSystem("TestActor")

  "TaskResultActor" should {
    "increment succeeded" in {
      val monitoringProbe = testKit.createTestProbe[MonitoringActor.Command]()
      val summaryProbe = testKit.createTestProbe[SummaryActor.Command]()
      val subject = testKit.spawn(
        TasksStateActor(
          SucceededTasks(List.empty),
          FailedTasks(List.empty),
          TimedOutTasks(List.empty)
        )
      )
      subject ! Succeeded("task1")
      subject ! AttemptToFinalise(monitoringProbe.ref, summaryProbe.ref)
      monitoringProbe
        .expectMessageType[AttemptResultSummary]
        .succeeded
        .events
        .length mustBe 1
    }

    "increment failed" in {
      val monitoringProbe = testKit.createTestProbe[MonitoringActor.Command]()
      val summaryProbe = testKit.createTestProbe[SummaryActor.Command]()
      val subject = testKit.spawn(
        TasksStateActor(
          SucceededTasks(List.empty),
          FailedTasks(List.empty),
          TimedOutTasks(List.empty)
        )
      )
      subject ! Failed("task1")
      subject ! AttemptToFinalise(monitoringProbe.ref, summaryProbe.ref)
      monitoringProbe
        .expectMessageType[AttemptResultSummary]
        .failed
        .events
        .length mustBe 1
    }

    "increment timedOut" in {
      val monitoringProbe = testKit.createTestProbe[MonitoringActor.Command]()
      val summaryProbe = testKit.createTestProbe[SummaryActor.Command]()
      val subject = testKit.spawn(
        TasksStateActor(
          SucceededTasks(List.empty),
          FailedTasks(List.empty),
          TimedOutTasks(List.empty)
        )
      )
      subject ! TimedOut("task1")
      subject ! AttemptToFinalise(monitoringProbe.ref, summaryProbe.ref)
      monitoringProbe
        .expectMessageType[AttemptResultSummary]
        .timedOut
        .events
        .length mustBe 1
    }

    "check state" in {
      val monitoringProbe = testKit.createTestProbe[MonitoringActor.Command]()
      val summaryProbe = testKit.createTestProbe[SummaryActor.Command]()
      val subject = testKit.spawn(
        TasksStateActor(
          SucceededTasks(List.empty),
          FailedTasks(List.empty),
          TimedOutTasks(List.empty)
        )
      )
      subject ! AttemptToFinalise(monitoringProbe.ref, summaryProbe.ref)
      monitoringProbe.expectMessageType[AttemptResultSummary]
    }

    "check state and finalise" in {
      val monitoringProbe = testKit.createTestProbe[MonitoringActor.Command]()
      val summaryProbe = testKit.createTestProbe[SummaryActor.Command]()
      val subject = testKit.spawn(
        TasksStateActor(
          SucceededTasks(List.empty),
          FailedTasks(List.empty),
          TimedOutTasks(List.empty)
        )
      )
      subject ! ForceFinalise(
        monitoringProbe.ref,
        summaryProbe.ref
      )
      monitoringProbe.expectMessageType[CompleteResultSummaryNow]
    }
  }
}
