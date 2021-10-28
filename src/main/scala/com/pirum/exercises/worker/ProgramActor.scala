package com.pirum.exercises.worker

import akka.actor.{ActorSystem, Timers}
import akka.pattern.after

import scala.collection.mutable
import scala.concurrent.{ExecutionContext, TimeoutException}
import scala.util.{Failure, Success}

case class ProcessTasks()

class ProgramActor(
    succeededTasksList: mutable.Buffer[String],
    failedTasksList: mutable.Buffer[String],
    timedOutTasksList: mutable.Buffer[String],
    tasks: List[Task]
)(implicit actorSystem: ActorSystem, executionContext: ExecutionContext)
    extends Timers {
  override def receive: Receive = { case ProcessTasks =>
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
  }
}
