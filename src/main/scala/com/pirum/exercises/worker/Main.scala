package com.pirum.exercises.worker

import akka.actor._
import com.pirum.exercises.worker.TaskResultActor.{
  CheckState,
  CheckStateAndFinalise
}

import java.util.concurrent.{Executors, TimeUnit}
import scala.concurrent.{ExecutionContext, TimeoutException}
import scala.concurrent.duration.FiniteDuration

object Main extends App with Program {
  implicit val system: ActorSystem = ActorSystem("worker-pool")

  def program(
      tasks: List[Task],
      timeout: FiniteDuration,
      workers: Int
  ): Unit = {
    val monitoringEC: ExecutionContext =
      ExecutionContext.fromExecutor(Executors.newFixedThreadPool(2))
    val tasksEC: ExecutionContext =
      ExecutionContext.fromExecutor(
        Executors.newFixedThreadPool(workers)
      )
    val taskResultActor = typed.ActorSystem(
      TaskResultActor.processCompletedTaskActor(
        SucceededTasks(List.empty),
        FailedTasks(List.empty),
        TimedOutTasks(List.empty)
      ),
      "task-result-actor"
    )
    val monitoringActor = typed.ActorSystem(
      MonitoringActor.summariseResultsActor(tasks),
      "monitoring-actor"
    )
    val programActor =
      system.actorOf(Props(new ProgramActor()(system, tasksEC)))
    tasks.foreach(task => programActor ! ProcessTask(task, taskResultActor))

    system.scheduler.scheduleOnce(timeout) {
      taskResultActor ! CheckStateAndFinalise(monitoringActor)
    }(monitoringEC)

    system.scheduler.scheduleAtFixedRate(
      FiniteDuration(0, TimeUnit.MILLISECONDS),
      FiniteDuration(1, TimeUnit.MILLISECONDS)
    )(() => taskResultActor ! CheckState(monitoringActor))(
      monitoringEC
    )
  }

  val tasks = List(
    Task("Task1", () => Thread.sleep(1000)),
    Task("Task2", () => Thread.sleep(6000)),
    Task("Task3", () => throw new UnsupportedOperationException),
    Task("Task4", () => Thread.sleep(6000)),
    Task("Task5", () => Thread.sleep(1000)),
    Task("Task6", () => Thread.sleep(1000)),
    Task("Task7", () => throw new UnsupportedOperationException),
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
  program(tasks, FiniteDuration(4, TimeUnit.SECONDS), tasks.size)
}
