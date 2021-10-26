package com.pirum.exercises.worker

import org.scalatest.concurrent.Eventually
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

import java.util.concurrent.TimeUnit
import scala.concurrent.duration.FiniteDuration

class TaskSpec extends AnyWordSpecLike with Matchers with Eventually {

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
      eventually(
        ThrowingTask(
          "test-task",
          FiniteDuration(1, TimeUnit.SECONDS)
        ).execute.failed
      )
    }
  }
}
