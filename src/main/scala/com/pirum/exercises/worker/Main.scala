package com.pirum.exercises.worker

import akka.actor.typed.ActorSystem
import com.pirum.exercises.worker.actors.RootActor.StartAll
import com.pirum.exercises.worker.actors._
import org.scalatest.time.SpanSugar.convertIntToGrainOfTime

import scala.concurrent.TimeoutException
import scala.concurrent.duration.FiniteDuration

object Main extends App with Program {

  def program(
      tasks: List[Task],
      timeout: FiniteDuration,
      workers: Int
  ): Unit = {
    val system: ActorSystem[RootActor.Command] = {
      ActorSystem(RootActor(tasks, timeout), "root-actor")
    }
    system ! StartAll()
  }

  val tasks = List(
    Task("Task1", () => Thread.sleep(1000)),
    Task("Task2", () => Thread.sleep(6000)),
    Task("Task3", () => throw new UnsupportedOperationException),
    Task("Task4", () => Thread.sleep(6000)),
    Task("Task5", () => Thread.sleep(1000)),
    Task("Task6", () => Thread.sleep(1000)),
    Task("Task7", () => throw new TimeoutException),
    Task("Task8", () => Thread.sleep(1000)),
    Task("Task9", () => Thread.sleep(1000)),
    Task("Task10", () => Thread.sleep(1000)),
    Task("Task11", () => Thread.sleep(1000)),
    Task("Task12", () => throw new UnsupportedOperationException),
    Task("Task13", () => Thread.sleep(6000)),
    Task("Task14", () => Thread.sleep(1000)),
    Task("Task15", () => Thread.sleep(1000)),
    Task("Task16", () => throw new UnsupportedOperationException)
  )
  program(tasks, 5.seconds, tasks.size)
}
