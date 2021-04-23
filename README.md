# Worker Pool coding exercise

Implement a program that dispatches tasks to a set of workers, collects and displays their result in the right order. The workers will concurrently execute these tasks and return the summary of the execution with tasks categorized as successful, failed or timed out. You are free to represent tasks any way you want as long as they expose the desired behaviour.

For example, the following input:

```
actions=[Task3(throws after 3s), Task4(compl. after 4s), Task2(compl. after 2s), Task1(throws after 1s)]
timeout=8s
workers=4
```

Should return the following result after 4 seconds:

```
result.successful = [Task2, Task4]
result.failed = [Task1, Task3]
result.timedOut = []
```

And the following input:

```
actions=[Task3(throws after 3s), Task5(hangs), Task4(compl. after 4s), Task2(compl. after 2s), Task1(compl. after 1s)]
timeout=8s
workers=4
```

Should return following result after 8 seconds:

```
result.successful = [Task1, Task2, Task4]
result.failed = [Task3]
result.timedOut = [Task5]
```

## Requirements:

- You need to execute tasks before timeout (given as a parameter). Collective duration of all the tasks will surely exceed the timeout, so you cannot execute tasks iteratively one after another.
- There will be something between 25 and 60 tasks to execute.
- Some of the tasks will work for some time and then finish. Other will fail and its run method will throw an unspecified exception. It can also happen that task hangs and does not return in short time. The program needs to categorize each passed task as successful, failed or timed out.
- Tasks must be returned in the order of their duration. It's guaranteed that when all tasks are started simultanoussly, each successful task will end in some distinct point in time and there should be no two tasks that finish at the same moment. Order of timed out tasks is not important.
- When there is no hanging task, the program should return after a time not significantly longer than the duration of the longest running task. For example, when the longest running task takes 3 seconds to execute, it should return after 3 seconds + maybe some small additional time.
- It is forbidden to use busy loops and `Thread.sleep`; all waits and blocks should be realized with proper methods.
