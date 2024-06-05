<!--
{
  "title": "Function Listeners",
  "id": "function-listener",
  "since": "6.1",
  "description": "This document explains how to use a function listeners in Lucee.",
  "keywords": [
    "parallel",
    "async",
    "thread",
    "function"
  ]
}
-->
# Function Listener

Lucee 6.1 introduced a new feature called "function listeners". This allows you to execute a function (built-in or user-defined) in parallel, so you do not have to wait for the result of the execution, similar to using the `cfthread` tag. Function listeners provide an easy syntax to not only execute the function in parallel but also include support to handle the result by simply adding a listener after the function call. This listener can handle the result or exception of a function. 

## Simple Version

This example demonstrates a simple function listener that executes `mySuccess` in parallel and sets a variable with the result.

```run
<cfscript>
function mySuccess() {
    return "Susi Sorglos";
}

mySuccess():function(result,error) {
    request.testFunctionListener=result;
};
// wait for the thread to finish
sleep(100);
dump(request.testFunctionListener ?: "undefined1");
</cfscript>
```

## Join thread thread

Instead of "run and forget" you can also join the thread with help of the function  `threadJoin` (or the tags `<cfthread action="join">`). You get a name from the call and that name you can use to join it. The thread information is available in the scope `cfhread` like a regular thread.

```run
<cfscript>
function mySuccess() {
    return "Susi Sorglos";
}
// the function call returns the name of the thread
threadName=mySuccess():function(result,error) {
    thread.result=result;
};
// you can then use the name to join the thread
threadJoin(threadName);

// and for example see the result in cfthread
dump(cfthread[threadName].result);
</cfscript>
```

## Handling Exceptions

In this case we see what happens when the function throws an exception. in that case the argument `error` is provided to the listener function, you can then for example send it to the log.

```run
<cfscript>
// function that throws an exception
function myError() {
    throw "Upsi dupsi!";
}

// storing the exception message in the thread scope
threadName=myError():function(result,error) {
    thread.result=error.message;
};

// wait for the thread to finish
threadJoin(threadName);

// see the result
dump(cfthread[threadName].result);
</cfscript>
```

## Listening on a Built-in Function (BIF)

Instead of user defined functions, you can also listen to build in functions as well.

```run
<cfscript>
threadName=arrayLen([1,2,3]):function(result,error) {
    thread.result=result;
};
// wait for the thread to finish
threadJoin(threadName);

dump(cfthread[threadName].result);
</cfscript>
```

## Listening on a variable "Chain"

You can also listen to variable "chain".

```run
<cfscript>
function mySuccess() {
    return "Susi Sorglos";
}
// create a chain
a.b.c.d=mySuccess;
threadName=a.b.c.d():function(result,error) {
    thread.result=result ?: error;
};
// wait for the thread to finish
threadJoin(threadName);

dump(cfthread[threadName].result);
</cfscript>
```


## Listening on a Component Instantiation

You can also listen to a component instantiation.

```run
<cfscript>
threadName=new Query():function(result,error) {
    thread.result=result;
};

// wait for the thread to finish
threadJoin(threadName);

dump(getMetadata(cfthread[threadName].result).fullname);
</cfscript>
```

## Listening on a Static Component Function 

You can also listen to a static component function.

```run
<cfscript>
threadName=Query::new(["columnName"]):function(result,error) {
    thread.result=result;
};

// wait for the thread to finish
threadJoin(threadName);

dump(cfthread[threadName].result.columnlist);
</cfscript>
```


## Function Collection Listener

A listener not necessarly has to be a function, it also can be a function collection (multiple functions inside a struct).
This way you can define a function for specific events like `onSuccess` or `onFaail` like this.

```run
<cfscript>
function mySuccess() {
    return "Susi Sorglos";
}

threadName1=mySuccess():{
    onSuccess:function(result) {
        thread.success=result;
    }
    ,onFail:function(error) {
        thread.fail=error.message;
    }
};

// wait for the thread to finish
threadJoin(threadName1);
dump(cfthread[threadName1].success);
</cfscript>
```


## Component Listener

You can also define a component instance as a listener, in this case we do a inline component.

```run
<cfscript>
function mySuccess() {
    return "Susi Sorglos";
}

threadName1=mySuccess():new component {  
    function onSuccess(result) {
        thread.success=result;
    }
};  

// wait for the thread to finish
threadJoin(threadName1);
dump(cfthread[threadName1].success);
</cfscript>
```


## No Listener

In case you wanna simply a asynchron exection, but you don't care about the outcome, you can define no listener at all, simply pass `null` (in this case i use the function `nullValue()`, because only with full null support enabled, the constant `null` is available).

```run
<cfscript>
// writing some data to the request scope
function logAndFail(name,value) {
    request.testFunctionListenerEcho[name]=value;
    throw "Upsi dupsi!";
}

threadName1=logAndFail("testNull","Peter Lustig"):nullValue();

// wait for the thread to finish
threadJoin(threadName1);

// reading the data stored to the request scope
dump(request.testFunctionListenerEcho.testNull);
</cfscript>
```

Instead of `null`, you can also simply pass a empty struct or a component not defining the function needed.

```coldfusion

// function collection with no listeners
threadName2=logAndFail("testStruct","Ruedi Zraggen"):{};

// function collection with no listeners
threadName2=logAndFail("testStruct","Ruedi Zraggen"):new component {};
```
