package com.pirum.exercises.worker.actors

import akka.actor.ActorSystem
import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import com.pirum.exercises.worker.Task
import com.pirum.exercises.worker.actors.ProgramActor.SubmitTask
import org.scalatest.wordspec.AnyWordSpecLike

class ProgramActorSpec extends ScalaTestWithActorTestKit with AnyWordSpecLike {
  implicit lazy val actorSystem: ActorSystem = ActorSystem("TestActor")

  "ProgramActor" should {
    "process a successful task" in {
      val probe = testKit.createTestProbe[TasksStateActor.Command]()
      val subject = testKit.spawn(
        ProgramActor(actorSystem.getDispatcher)
      )
      subject ! SubmitTask(Task("Task1", () => Thread.sleep(100)), probe.ref)
      probe.expectMessage(TasksStateActor.Succeeded("Task1"))
    }

    "process a throwing task" in {
      val probe = testKit.createTestProbe[TasksStateActor.Command]()
      val subject = testKit.spawn(
        ProgramActor(actorSystem.getDispatcher)
      )
      subject ! SubmitTask(
        Task("Task1", () => throw new UnsupportedOperationException),
        probe.ref
      )
      probe.expectMessage(TasksStateActor.Failed("Task1"))
    }
  }
}
