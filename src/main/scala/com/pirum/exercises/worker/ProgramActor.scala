package com.pirum.exercises.worker

import akka.actor.{ActorSystem, PoisonPill, Timers}
import akka.pattern.after

import scala.collection.mutable
import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{ExecutionContext, TimeoutException}
import scala.util.{Failure, Success}

sealed trait Command
case class ProcessTasks() extends Command
case class DisplayResults() extends Command
case class AwaitResults() extends Command

class ProgramActor(
    succeededTasksList: mutable.Buffer[String],
    failedTasksList: mutable.Buffer[String],
    timedOutTasksList: mutable.Buffer[String],
    tasks: List[Task],
    totalTimeout: FiniteDuration
)(implicit actorSystem: ActorSystem, executionContext: ExecutionContext)
    extends Timers {
  override def receive: Receive = {
    case ProcessTasks =>
      tasks.foreach(task => {
        after(task.delay)(task.execute)
          .onComplete {
            case Success(_) => succeededTasksList += task.name
            case Failure(e) =>
              e match {
                case _: TimeoutException => timedOutTasksList += task.name
                case _: UnsupportedOperationException =>
                  failedTasksList += task.name
              }
          }
      })
    case DisplayResults =>
      timers.startSingleTimer(
        "display-result-task",
        AwaitResults,
        totalTimeout
      )
    case AwaitResults =>
      tasks
        .filter(task => !succeededTasksList.contains(task.name))
        .filter(task => !failedTasksList.contains(task.name))
        .filter(task => !timedOutTasksList.contains(task.name))
        .foreach(task => timedOutTasksList += task.name)
      println(
        s"result.successful = [${succeededTasksList.map(taskName => s"$taskName").mkString(", ")}]"
      )
      println(
        s"result.failed = [${failedTasksList.map(taskName => s"$taskName").mkString(", ")}]"
      )
      println(
        s"result.timedOut = [${timedOutTasksList.map(taskName => s"$taskName").mkString(", ")}]"
      )
      self ! PoisonPill
  }
}
