package com.pirum.exercises.worker

import akka.actor.{ActorSystem, PoisonPill, Timers}

import java.util.concurrent.TimeUnit
import scala.collection.mutable
import scala.concurrent.ExecutionContext
import scala.concurrent.duration.FiniteDuration

sealed trait Command
case class DisplayResults() extends Command
case class AwaitResults() extends Command
case class MonitorResults(startNano: Long) extends Command

class MonitoringActor(
    succeededTasksList: mutable.Buffer[String],
    failedTasksList: mutable.Buffer[String],
    timedOutTasksList: mutable.Buffer[String],
    tasks: List[Task],
    timeout: FiniteDuration
)(implicit actorSystem: ActorSystem, executionContext: ExecutionContext)
    extends Timers {

  var isDone: Boolean = false

  override def receive: Receive = {
    case DisplayResults =>
      val startMilli = System.currentTimeMillis()
      timers.startTimerAtFixedRate(
        "display-result-task",
        MonitorResults(startMilli),
        FiniteDuration(0, TimeUnit.NANOSECONDS),
        FiniteDuration(1, TimeUnit.NANOSECONDS)
      )
    case MonitorResults(startMilli) =>
      if (allTasksProcessed() && !isDone) {
        isDone = true
        printResults()
        self ! PoisonPill
      } else if (
        (System.currentTimeMillis() - startMilli > timeout.toMillis) && !isDone
      ) {
        isDone = true
        tasks
          .filter(task => !succeededTasksList.contains(task.name))
          .filter(task => !failedTasksList.contains(task.name))
          .filter(task => !timedOutTasksList.contains(task.name))
          .foreach(task => timedOutTasksList += task.name)
        printResults()
        self ! PoisonPill
      }
  }

  private def printResults(): Unit = {
    println(
      s"result.successful = [${succeededTasksList.map(taskName => s"$taskName").mkString(", ")}]"
    )
    println(
      s"result.failed = [${failedTasksList.map(taskName => s"$taskName").mkString(", ")}]"
    )
    println(
      s"result.timedOut = [${timedOutTasksList.map(taskName => s"$taskName").mkString(", ")}]"
    )
  }

  private def allTasksProcessed(): Boolean = {
    succeededTasksList.length +
      failedTasksList.length +
      timedOutTasksList.length == tasks.length
  }
}
