package com.pirum.exercises.worker

import akka.actor.{ActorSystem, Props}
import akka.testkit.TestProbe
import org.scalatest.concurrent.Eventually
import org.scalatest.matchers.must.Matchers.{be, convertToAnyMustWrapper}
import org.scalatest.time.{Seconds, Span}
import org.scalatest.wordspec.AnyWordSpecLike

import java.util.concurrent.TimeUnit
import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.FiniteDuration

class ProgramActorSpec extends AnyWordSpecLike with Eventually {
  implicit lazy val system: ActorSystem = ActorSystem("TestActor")
  override implicit val patienceConfig: PatienceConfig = PatienceConfig(
    Span(5, Seconds)
  )

  "ProgramActor" should {
    "process a successful task" in {
      val succeededTasksList: mutable.Buffer[String] = ListBuffer[String]()
      val failedTasksList: mutable.Buffer[String] = ListBuffer[String]()
      val timedOutTasksList: mutable.Buffer[String] = ListBuffer[String]()
      val parent = TestProbe()
      val child = parent.childActorOf(
        Props(
          new ProgramActor(
            succeededTasksList,
            failedTasksList,
            timedOutTasksList,
            List(SuccessfulTask("task1", FiniteDuration(1, TimeUnit.SECONDS))),
            FiniteDuration(1, TimeUnit.SECONDS)
          )
        )
      )
      child ! ProcessTasks
      eventually {
        succeededTasksList.length must be(1)
      }
    }

    "process a throwing task" in {
      val succeededTasksList: mutable.Buffer[String] = ListBuffer[String]()
      val failedTasksList: mutable.Buffer[String] = ListBuffer[String]()
      val timedOutTasksList: mutable.Buffer[String] = ListBuffer[String]()
      val parent = TestProbe()
      val child = parent.childActorOf(
        Props(
          new ProgramActor(
            succeededTasksList,
            failedTasksList,
            timedOutTasksList,
            List(ThrowingTask("task1", FiniteDuration(1, TimeUnit.SECONDS))),
            FiniteDuration(1, TimeUnit.SECONDS)
          )
        )
      )
      child ! ProcessTasks
      eventually {
        failedTasksList.length must be(1)
      }
    }

    "process a timeout task" in {
      val succeededTasksList: mutable.Buffer[String] = ListBuffer[String]()
      val failedTasksList: mutable.Buffer[String] = ListBuffer[String]()
      val timedOutTasksList: mutable.Buffer[String] = ListBuffer[String]()
      val parent = TestProbe()
      val child = parent.childActorOf(
        Props(
          new ProgramActor(
            succeededTasksList,
            failedTasksList,
            timedOutTasksList,
            List(TimeoutTask("task1", FiniteDuration(1, TimeUnit.SECONDS))),
            FiniteDuration(1, TimeUnit.SECONDS)
          )
        )
      )
      child ! ProcessTasks
      eventually {
        timedOutTasksList.length must be(1)
      }
    }

    "process a successful, timeout and throwing tasks" in {
      val succeededTasksList: mutable.Buffer[String] = ListBuffer[String]()
      val failedTasksList: mutable.Buffer[String] = ListBuffer[String]()
      val timedOutTasksList: mutable.Buffer[String] = ListBuffer[String]()
      val parent = TestProbe()
      val child = parent.childActorOf(
        Props(
          new ProgramActor(
            succeededTasksList,
            failedTasksList,
            timedOutTasksList,
            List(
              SuccessfulTask("task1", FiniteDuration(1, TimeUnit.SECONDS)),
              ThrowingTask("task2", FiniteDuration(1, TimeUnit.SECONDS)),
              TimeoutTask("task3", FiniteDuration(1, TimeUnit.SECONDS))
            ),
            FiniteDuration(1, TimeUnit.SECONDS)
          )
        )
      )
      child ! ProcessTasks
      eventually {
        timedOutTasksList.length must be(1)
        failedTasksList.length must be(1)
        succeededTasksList.length must be(1)
      }
    }
  }
}
