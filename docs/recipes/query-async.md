<!--
{
  "title": "Query Async",
  "id": "query-async",
  "since": "6.0",
  "description": "Learn how to execute queries asynchronously in Lucee. This guide demonstrates how to set up asynchronous query execution using a simple flag. Examples include defining async execution for queries and using local listeners to handle exceptions. Additionally, function listeners introduced in Lucee 6.1 can be used for this purpose.",
  "keywords": [
    "query",
    "async",
    "listener",
    "thread",
    "parallel"
  ]
}
-->
# Query Async

Since Lucee 6.0, you can define that a query gets executed asynchronously. Asynchronous execution of queries is useful in many cases where you don’t want to wait for a query to be executed. You can now set a simple flag to enable this feature.

## Async Execution

This example shows how to define a query for async execution:

```lucee
query async=true {
~~~
update user set lastAccess=now()
~~~ 
}
```

But you may want to know when an exception is happening, so we use a local listener:

```lucee
query datasource="mysql" async=true listener={
    error: function (args, caller, meta, exception) {
        systemOutput(exception, true, true);
    }
} {
~~~
    update user set lastAccess=now()
~~~
}
```

## Other Option

Since Lucee 6.1, this can also be done with the help of "function listeners".