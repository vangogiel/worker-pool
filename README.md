# worker-pool

A program that dispatches tasks to a set of workers, collects and displays their result in the right order. The workers will concurrently execute these tasks and return the summary of the execution with tasks categorized as successful, failed or timed out. The failed tasks are categorised upon throwing an exception. 

The global timeout will be used to summarie the results of the tasks. The program should return the following result after X seconds:

```
result.successful = [Task2, Task4]
result.failed = [Task1, Task3]
result.timedOut = []
```

The user is free to specify tasks they desire as per setup in the Main.scala.

