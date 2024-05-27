<!--
{
  "title": "Thread Task",
  "id": "thread_task",
  "related": [
    "tag-thread"
  ],
  "categories": [
    "thread"
  ],
  "description": "How to use Thread Tasks",
  "keywords": [
    "Thread Tasks",
    "Daemon Threads",
    "Task Threads",
    "Retry",
    "Lucee"
  ]
}
-->
## Thread Task ##

This document explains about the thread tasks. It is useful to know the differences between regular threads and task threads.

When a regular thread throws an exception, the default exception type is `type='daemon'`. After an exception, the thread is dumped. If a regular thread is terminated due to an error, the information from the thread will be output with a 'terminated' status.

Regular Threads have the following characteristics:

1) **Bound to current request**: With the help of CFThread you can always see what the thread is doing. With `action='join'` you can wait until the thread ends and join it. You can also call `action='terminate'` and end the thread. You always have control over the thread with the various actions.

2) **Runs only once**: The thread runs only once. It ends at the end of the Cfthread tag or if there is an exception.

3) **It fails when it fails**: There is no special exception handling so when the thread fails it fails unless you have Cftry, cfcatch inside the thread and you have exception handling there.

### Example 1: ###

In addition to daemon (regular) threads, Lucee also supports task threads. The main differences is that task threads execute completely independently of the request that is calling the thread. We log the thread and can re-execute it.

This example shows a task thread. It is similar to the daemon thread, but we do not have the join and output of the thread because these are not allowed with a task thread.

```luceescript
thread name="test" type="daemon" {
	throw "hopala!";
}
thread action="join" name="test";
dump(cfthread.test);
```

Note that when you execute the example code, you will get no output. This is expected since no output was written in the code.

However, view the Lucee _Admin --> Services --> Tasks_ and see the name of the tasks and their `Type` is `daemon` and the status is `terminated`.

### Example 2: ###

Next we show a similar example using the task type:

```luceescript
thread name="test" type="task" {
	throw "hopala!";
}
sleep(1000);
dump(getPageContext().getTasks().getTask("test"));
```

This example shows the task type. This example is similar to the daemon thread. It waits 1 second before outputting the task details. The task type thread will continue to run independently.

### Example 3: ###

Task threads can be retried multiple times if they fail. This is different from daemon threads which fail permanently after an exception. Below example shows a task thread that retries multiple times.

```luceescript
thread name="test" type="task"
	retryinterval=[
		{tries:3, interval:createTimeSpan(0,0,0,1)},
		{tries:5, interval:createTimeSpan(0,0,0,5)},
		{tries:10, interval:createTimeSpan(0,0,0,10)},
		{tries:10, interval:createTimeSpan(0,0,1,0)},
		{tries:20, interval:createTimeSpan(0,0,10,0)}
	] {
	throw "hopala!";
}
sleep(1000);
dump(getPageContext().getTasks().getTask("test"));
```

This example creates a task thread named `test` that retries according to the defined intervals. Initially, the task thread throws an exception. The retry intervals are defined in the array with `tries` and `interval` attributes.

### Example 4: ###

Another example for getting the admin component and task is physically created on the system.

```luceescript
thread name="test" type="task"
	retryinterval=[
		{tries:3, interval:createTimeSpan(0,0,0,1)},
		{tries:5, interval:createTimeSpan(0,0,0,5)},
		{tries:10, interval:createTimeSpan(0,0,0,10)},
		{tries:10, interval:createTimeSpan(0,0,1,0)},
		{tries:20, interval:createTimeSpan(0,0,10,0)}
	] {
	throw "hopala!";
}
sleep(1000);
admin=new Administrator(type:"web",password:"server");
tasks=admin.getTasks();
dump(tasks);
admin.executeTask(tasks.id);
admin.removeTask(tasks.id[tasks.recordcount]);
admin.removeAllTask();
```

1) `admin.getTasks()` is used to list out all existing tasks. When executed, it returns a query that contains the information from the task.

2) `admin.executeTask()` is used to execute the task and we see it in the browser. It throws an exception.

3) `admin.removeTask()` and `admin.removeAllTask()` are used to remove tasks from the administrator.

### Footnotes ###

Here you can see the details in the video:
[Thread Task](https://youtu.be/-SUbVWqJRME)