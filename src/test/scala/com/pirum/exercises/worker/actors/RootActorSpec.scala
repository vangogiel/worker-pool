package com.pirum.exercises.worker.actors

import akka.actor.ActorSystem
import akka.actor.testkit.typed.Effect
import akka.actor.testkit.typed.Effect.Spawned
import akka.actor.testkit.typed.scaladsl.{
  BehaviorTestKit,
  ManualTime,
  ScalaTestWithActorTestKit,
  TestProbe
}
import akka.actor.typed.Behavior
import com.pirum.exercises.worker.Task
import com.pirum.exercises.worker.actors.RootActor.StartAll
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import org.scalatest.matchers.should.Matchers
import org.scalatest.time.SpanSugar.convertIntToGrainOfTime
import org.scalatest.wordspec.AnyWordSpecLike

import java.util.concurrent.TimeUnit
import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.FiniteDuration

class RootActorSpec
    extends ScalaTestWithActorTestKit(ManualTime.config)
    with Matchers
    with AnyWordSpecLike {
  implicit lazy val actorSystem: ActorSystem = ActorSystem("TestActor")

  val manualTime: ManualTime = ManualTime()

  val tasks: List[Task] =
    List(Task("Task1", () => None), Task("Task2", () => None))
  val monitoringProbe: TestProbe[MonitoringActor.Command] =
    testKit.createTestProbe[MonitoringActor.Command]()
  val programProbe: TestProbe[ProgramActor.Command] =
    testKit.createTestProbe[ProgramActor.Command]()
  val summaryProbe: TestProbe[SummaryActor.Command] =
    testKit.createTestProbe[SummaryActor.Command]()
  val tasksStateProbe: TestProbe[TasksStateActor.Command] =
    testKit.createTestProbe[TasksStateActor.Command]()

  override def afterAll(): Unit = testKit.shutdownTestKit()

  "RootActor timed behavior" should {
    val subject = BehaviorTestKit(
      RootActor.getTimedBehavior(
        tasks,
        programProbe.ref,
        tasksStateProbe.ref,
        monitoringProbe.ref,
        summaryProbe.ref,
        1.second
      )
    )

    subject.run(StartAll())

    "run the setup and schedule all tasks correctly" in {
      subject
        .expectEffectType[Effect.TimerScheduled[RootActor.StartProgram]]
        .delay mustBe FiniteDuration(0, TimeUnit.SECONDS)
      subject
        .expectEffectType[Effect.TimerScheduled[RootActor.StartMonitoring]]
        .delay mustBe FiniteDuration(1, TimeUnit.MILLISECONDS)
      subject
        .expectEffectType[Effect.TimerScheduled[RootActor.FinaliseNow]]
        .delay mustBe FiniteDuration(1, TimeUnit.SECONDS)
    }
  }

  "RootActor scheduled tasks" should {
    val rootActorRef = testKit.spawn(
      RootActor.getTimedBehavior(
        tasks,
        programProbe.ref,
        tasksStateProbe.ref,
        monitoringProbe.ref,
        summaryProbe.ref,
        0.seconds
      )
    )
    rootActorRef ! StartAll()

    "submit tasks to the ProgramActor" in {
      programProbe.expectMessageType[ProgramActor.SubmitTask]
    }

    "start monitoring the state of tasks" in {
      tasksStateProbe.expectMessageType[TasksStateActor.AttemptToFinalise]
    }

    "finalise the results of the tasks after a delay" in {
      tasksStateProbe.expectMessageType[TasksStateActor.ForceFinalise]
    }
  }
}
