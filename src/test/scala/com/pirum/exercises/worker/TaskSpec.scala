package com.pirum.exercises.worker

import org.scalatest.concurrent.Eventually
import org.scalatest.concurrent.ScalaFutures.whenReady
import org.scalatest.matchers.must.Matchers
import org.scalatest.time.{Seconds, Span}
import org.scalatest.wordspec.AnyWordSpecLike

import java.util.concurrent.TimeUnit
import scala.concurrent.TimeoutException
import scala.concurrent.duration.FiniteDuration
import scala.language.postfixOps

class TaskSpec extends AnyWordSpecLike with Matchers with Eventually {
  override implicit val patienceConfig: PatienceConfig = PatienceConfig(
    Span(5, Seconds)
  )

  "Task" should {
    "succeed if SuccessfulTask is specified" in {
      eventually(
        SuccessfulTask(
          "test-task",
          FiniteDuration(1, TimeUnit.SECONDS)
        ).execute.isCompleted
      )
    }

    "fail if ThrowingTask is specified" in {
      whenReady(
        ThrowingTask(
          "test-task",
          FiniteDuration(1, TimeUnit.SECONDS)
        ).execute.failed
      ) { e => e mustBe an[UnsupportedOperationException] }
    }

    "timeout if TimeoutTask is specified" in {
      whenReady(
        TimeoutTask(
          "test-task",
          FiniteDuration(1, TimeUnit.SECONDS)
        ).execute.failed
      ) { e => e mustBe an[TimeoutException] }
    }
  }
}
