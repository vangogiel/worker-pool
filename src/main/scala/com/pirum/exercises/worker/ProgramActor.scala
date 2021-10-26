package com.pirum.exercises.worker

import akka.actor.{Actor, ActorSystem}
import akka.pattern.after

import scala.collection.mutable
import scala.concurrent.ExecutionContext
import scala.concurrent.duration.FiniteDuration
import scala.util.{Failure, Success}

sealed trait Command
case class ProcessTasks() extends Command

class ProgramActor(
    succeededTasksList: mutable.Buffer[String],
    failedTasksList: mutable.Buffer[String],
    tasks: List[Task],
    totalTimeout: FiniteDuration
)(implicit actorSystem: ActorSystem, executionContext: ExecutionContext)
    extends Actor {
  override def receive: Receive = { case ProcessTasks =>
    tasks.foreach(task => {
      if (task.delay.compare(totalTimeout) < 1) {
        after(task.delay)(task.execute)
          .onComplete {
            case Success(_) => succeededTasksList += task.name
            case Failure(_) => failedTasksList += task.name
          }
      }
    })
  }
}
