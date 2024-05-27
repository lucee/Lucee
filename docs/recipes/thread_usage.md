<!--
{
  "title": "Threads",
  "id": "thread_usage",
  "categories": [
    "scopes",
    "thread"
  ],
  "description": "How to use threads in Lucee",
  "keywords": [
    "Threads",
    "Parallel execution",
    "cfthread",
    "Asynchronous tasks",
    "Lucee"
  ]
}
-->
## Thread Scope

This document explains how to use threads in Lucee. Threads are mainly used for executing code in parallel.

### Example 1

The below example shows normal execution, waiting for all code to resolve. Here it takes 1000 milliseconds to complete the execution.

```lucee
<cfscript>
function napASecond() localmode=true {
    sleep(1000);
}
start = getTickCount();
napASecond();
dump("done in #getTickCount()-start#ms");
</cfscript>
```

The below example shows thread execution. Code within the thread tag runs in parallel with the execution of the other code in the cfscript. Here execution does not wait for sleep.

```lucee
<cfscript>
function napASecond() localmode=true {
    thread {
        sleep(1000);
    }
}
start = getTickCount();
napASecond();
dump("done in #getTickCount()-start#ms");
</cfscript>
```

Threads run independently of other threads or code in Lucee. Lucee does not the thread first and then the following code.

Threads are mainly used to retrieve data from the database, cfhttp, or webservice. Threads are used when it does not matter how much time it takes to execute.

### Example 2

Here we see an example using multiple threads in Lucee. All threads run in parallel.

```lucee
<cfscript>
function napASecond(index) localmode=true {
    thread {
        thread.start = now();
        sleep(1000)
        thread.end = now();
    }
}

start = getTickCount();
loop from=1 to=5 index="index" {
    napASecond(index);
}

// show threads
dump(var:cfthread, expand=false);
// wait for all threads to finish
thread action="join" name=cfthread.keyList();
// show threads
dump(var:cfthread, expand=false);

dump("done in #getTickCount()-start#ms");
</cfscript>
```

The above example shows five threads running in parallel. The thread action="join" line waits for all threads to finish. `cfthread` returns struct info of all thread statuses.

### Example 3

Threads can also be used to perform long-running tasks asynchronously.

```lucee
<cfscript>
// normal array
list = ["Susi", "Harry"];
list.each(
    function(value, key, struct) {
        systemOutput(arguments, true);
    }
    ,true
);
// struct
list = {"name": "Susi", "age": 47};
list.each(
    function(value, key, struct) {
        systemOutput(arguments, true);
    }
    ,true
);
// list
list = "Susi,Harry";
list.each(
    function(value, key, struct) {
        systemOutput(arguments, true);
    }
    ,true
);
// query
persons = query(
    'firstName': ['Susi', 'Harry'],
    'lastName': ['Sorglos', 'Hirsch']
);
queryEach(
    persons,
    function(value, row, query) {
        systemOutput(arguments, true);
    }
    ,true
);
persons.each(
    function(value, row, query) {
        systemOutput(arguments, true);
    }
    ,true
);
dump("done");
</cfscript>
```

### Example 4

Lucee members often discuss how to extend functionality to make Lucee easier to use or adding other new functionality.

This example shows a future implementation of threads in Lucee.

```lucee
<cfscript>
// Thread Pool
tasks.each(
    function(value, key, struct) {
        systemOutput(arguments, true);
    }
    ,true
    ,20 // ATM default for max threads is 20, instead we plan to use a smart thread pool in the future (Java ExecutorService)
);
</cfscript>
```

Currently, the default max threads is 20. In the future, we plan to use a smart thread pool based on your JVM (Java ExecutorService). So you will not have to take care of how many threads are being used. The system will do that, and provide the best choice for your code.

```lucee
<cfscript>
thread /* action="thread" name="whatever" */ {
    sleep();
}
</cfscript>
```

In the future, we will make threads smarter by also using a pool for threads.

This feature is something we are planning. Changes will be implemented on the backend so that nothing changes on the front end.

```lucee
<cfscript>
// Extend parallel
loop from=1 to=10 index="i" parallel=true {
    ...
}
// ???
for(i=0; i<10; i++; true) {
}
</cfscript>
```

Another planned enhancement is to extend parallel to the loop by simply adding `parallel=true`. It will execute the body of the loop in parallel.

### Footnotes

Here you can see the above details in a video:

[Lucee Threads](https://www.youtube.com/watch?v=oGUZRrcg9KE)