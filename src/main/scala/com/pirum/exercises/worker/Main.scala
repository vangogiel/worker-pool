package com.pirum.exercises.worker

import akka.actor._
import com.pirum.exercises.worker.TaskResultActor.{
  CheckState,
  CheckStateAndFinalise
}

import java.util.concurrent.{Executors, TimeUnit}
import scala.concurrent.ExecutionContext
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
    SuccessfulTask("Task1", FiniteDuration(1, TimeUnit.SECONDS)),
    SuccessfulTask("Task2", FiniteDuration(6, TimeUnit.SECONDS)),
    ThrowingTask("Task3", FiniteDuration(1, TimeUnit.SECONDS)),
    TimeoutTask("Task4", FiniteDuration(1, TimeUnit.SECONDS)),
    SuccessfulTask("Task5", FiniteDuration(1, TimeUnit.SECONDS)),
    SuccessfulTask("Task6", FiniteDuration(1, TimeUnit.SECONDS)),
    ThrowingTask("Task7", FiniteDuration(1, TimeUnit.SECONDS)),
    SuccessfulTask("Task8", FiniteDuration(1, TimeUnit.SECONDS)),
    SuccessfulTask("Task9", FiniteDuration(1, TimeUnit.SECONDS)),
    SuccessfulTask("Task10", FiniteDuration(1, TimeUnit.SECONDS)),
    SuccessfulTask("Task11", FiniteDuration(1, TimeUnit.SECONDS)),
    ThrowingTask("Task12", FiniteDuration(1, TimeUnit.SECONDS)),
    TimeoutTask("Task13", FiniteDuration(1, TimeUnit.SECONDS)),
    SuccessfulTask("Task14", FiniteDuration(1, TimeUnit.SECONDS)),
    SuccessfulTask("Task15", FiniteDuration(1, TimeUnit.SECONDS)),
    ThrowingTask("Task16", FiniteDuration(1, TimeUnit.SECONDS))
  )
  program(tasks, FiniteDuration(4, TimeUnit.SECONDS), tasks.size)
}
