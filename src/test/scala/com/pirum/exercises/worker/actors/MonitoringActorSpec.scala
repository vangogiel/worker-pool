package com.pirum.exercises.worker.actors

import akka.actor.ActorSystem
import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import com.pirum.exercises.worker.Task
import com.pirum.exercises.worker.actors.MonitoringActor.{
  AttemptResultSummary,
  CompleteResultSummaryNow
}
import org.scalatest.wordspec.AnyWordSpecLike

class MonitoringActorSpec
    extends ScalaTestWithActorTestKit
    with AnyWordSpecLike {
  implicit lazy val actorSystem: ActorSystem = ActorSystem("TestActor")

  "MonitoringActor" should {
    "attempt to fetch the results when all tasks are completed" in {
      val task1 = Task("Task1", () => None)
      val task2 = Task("Task2", () => None)

      val probe = testKit.createTestProbe[SummaryActor.Command]()
      val subject = testKit.spawn(
        MonitoringActor(List(task1, task2))
      )

      subject ! AttemptResultSummary(
        SucceededTasks(List("Task1")),
        FailedTasks(List("Task2")),
        TimedOutTasks(List.empty),
        probe.ref
      )

      probe.expectMessage(
        SummaryActor.PrintResults(
          SucceededTasks(List("Task1")),
          FailedTasks(List("Task2")),
          TimedOutTasks(List.empty)
        )
      )
    }

    "not attempt to fetch the results when some tasks are not completed" in {
      val task1 = Task("Task1", () => None)
      val task2 = Task("Task2", () => None)

      val probe = testKit.createTestProbe[SummaryActor.Command]()
      val subject = testKit.spawn(
        MonitoringActor(List(task1, task2))
      )

      subject ! AttemptResultSummary(
        SucceededTasks(List("Task1")),
        FailedTasks(List.empty),
        TimedOutTasks(List.empty),
        probe.ref
      )

      probe.expectNoMessage()
    }

    "attempt to fetch the results at anytime when asked" in {
      val task1 = Task("Task1", () => None)
      val task2 = Task("Task2", () => None)

      val probe = testKit.createTestProbe[SummaryActor.Command]()
      val subject = testKit.spawn(
        MonitoringActor(List(task1, task2))
      )

      subject ! CompleteResultSummaryNow(
        SucceededTasks(List("Task1")),
        FailedTasks(List.empty),
        probe.ref
      )

      probe.expectMessage(
        SummaryActor.PrintResults(
          SucceededTasks(List("Task1")),
          FailedTasks(List.empty),
          TimedOutTasks(List("Task2"))
        )
      )
    }
  }
}
