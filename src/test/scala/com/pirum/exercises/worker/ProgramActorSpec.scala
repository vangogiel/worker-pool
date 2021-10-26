package com.pirum.exercises.worker

import akka.actor.{ActorSystem, Props}
import akka.testkit.TestProbe
import org.scalatest.concurrent.Eventually
import org.scalatest.matchers.must.Matchers
import org.scalatest.time.{Seconds, Span}
import org.scalatest.wordspec.AnyWordSpecLike

import java.util.concurrent.TimeUnit
import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.FiniteDuration

class ProgramActorSpec extends AnyWordSpecLike with Matchers with Eventually {
  implicit lazy val system: ActorSystem = ActorSystem("TestActor")
  override implicit val patienceConfig: PatienceConfig = PatienceConfig(
    Span(5, Seconds)
  )

  "ProgramActor" should {
    "process a successful task" in {
      val succeededTasksList: mutable.Buffer[String] = ListBuffer[String]()
      val failedTasksList: mutable.Buffer[String] = ListBuffer[String]()
      val parent = TestProbe()
      val child = parent.childActorOf(
        Props(
          new ProgramActor(
            succeededTasksList,
            failedTasksList,
            List(SuccessfulTask("task1", FiniteDuration(1, TimeUnit.SECONDS))),
            FiniteDuration(3, TimeUnit.SECONDS)
          )
        )
      )
      child ! ProcessTasks
      eventually {
        succeededTasksList.length must be(1)
        failedTasksList.length must be(0)
      }
    }

    "process a throwing task" in {
      val succeededTasksList: mutable.Buffer[String] = ListBuffer[String]()
      val failedTasksList: mutable.Buffer[String] = ListBuffer[String]()
      val parent = TestProbe()
      val child = parent.childActorOf(
        Props(
          new ProgramActor(
            succeededTasksList,
            failedTasksList,
            List(ThrowingTask("task1", FiniteDuration(1, TimeUnit.SECONDS))),
            FiniteDuration(3, TimeUnit.SECONDS)
          )
        )
      )
      child ! ProcessTasks
      eventually {
        succeededTasksList.length must be(0)
        failedTasksList.length must be(1)
      }
    }
  }
}
