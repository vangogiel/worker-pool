package com.pirum.exercises.worker

// A task that either succeeds after n seconds, fails after n seconds, or never terminates
sealed trait Task {
  def execute: Future[Unit]
}
