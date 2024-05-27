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

Lucee 6.1 introduced a new feature called "function listeners". This allows you to execute a function (built-in or user-defined) in parallel, so you do not have to wait for the result of the execution, similar to using the `cfthread` tag. Function listeners provide an easy syntax to not only execute the function in parallel but also include support to handle the result by simply adding a listener after the function call. This listener can handle the result or exception of a function. Following are some examples with explanations.

## Examples

### Simple Version

This example demonstrates a simple function listener that executes `mySuccess` in parallel and sets a variable with the result.

```coldfusion
function mySuccess() {
    return "Susi Sorglos";
}

var t=mySuccess():function(result,error) {
    variables.testFunctionListenerV=result;
    thread.testFunctionListenerV=result;
};
// wait for the thread to finish
sleep(100);
dump(cfthread[t].testFunctionListenerV ? "undefined1");
```

### Listening on a UDF that Does Not Fail (Joining the Thread)

This example demonstrates listening on a user-defined function (UDF) that does not fail and waiting for the thread to finish using `threadJoin`.

```coldfusion
function mySuccess() {
    return "Susi Sorglos";
}

var threadName=mySuccess():function(result,error) {
    thread.result=result;
};
// wait for the thread to finish
threadJoin(threadName);

dump(cfthread[threadName].result);
```

### Listening on a UDF that Does Fail (Joining the Thread)

This example demonstrates listening on a user-defined function (UDF) that fails and waiting for the thread to finish using `threadJoin`.

```coldfusion
function myError() {
    throw "Upsi dupsi!";
}

var threadName=myError():function(result,error) {
    thread.result=error.message;
};
// wait for the thread to finish
threadJoin(threadName);

dump(cfthread[threadName].result);
```

### Listening on a Built-in Function (BIF) that Does Not Fail (Joining the Thread)

This example demonstrates listening on a built-in function (BIF) that does not fail and waiting for the thread to finish using `threadJoin`.

```coldfusion
var threadName=arrayLen([1,2,3]):function(result,error) {
    thread.result=result;
};
// wait for the thread to finish
threadJoin(threadName);

dump(cfthread[threadName].result);
```

### Listening on a UDF Chain that Does Not Fail (Joining the Thread)

This example demonstrates listening on a chain of user-defined functions (UDFs) that does not fail and waiting for the thread to finish using `threadJoin`.

```coldfusion
function mySuccess() {
    return "Susi Sorglos";
}

a.b.c.d=mySuccess;
var threadName=a.b.c.d():function(result,error) {
    thread.result=result ? error;
};
// wait for the thread to finish
threadJoin(threadName);
dump(cfthread[threadName].result);
```

### Listening on a Local UDF Chain that Does Not Fail (Joining the Thread)

This example demonstrates listening on a chain of local user-defined functions (UDFs) that does not fail and waiting for the thread to finish using `threadJoin`.

```coldfusion
function mySuccess() {
    return "Susi Sorglos";
}

local.a.b.c.d=mySuccess;
var threadName=local.a.b.c.d():function(result,error) {
    thread.result=result ? error;
};
// wait for the thread to finish
threadJoin(threadName);
dump(cfthread[threadName].result);
```

### Listening on a Variables UDF Chain that Does Not Fail (Joining the Thread)

This example demonstrates listening on a chain of user-defined functions (UDFs) that does not fail and waiting for the thread to finish using `threadJoin`.

```coldfusion
function mySuccess() {
    return "Susi Sorglos";
}

variables.a.b.c.d=mySuccess;
var threadName=variables.a.b.c.d():function(result,error) {
    thread.result=result ? error;
};
// wait for the thread to finish
threadJoin(threadName);
dump(cfthread[threadName].result);
```

### Listening on an Arguments UDF Chain that Does Not Fail (Joining the Thread)

This example demonstrates listening on a chain of user-defined functions (UDFs) passed as arguments that does not fail and waiting for the thread to finish using `threadJoin`.

```coldfusion
function mySuccess() {
    return "Susi Sorglos";
}

arguments.a.b.c.d=mySuccess;
var threadName=arguments.a.b.c.d():function(result,error) {
    thread.result=result ? error;
};
// wait for the thread to finish
threadJoin(threadName);
dump(cfthread[threadName].result);
```

### Listening on a Component Instantiation (No Package) that Does Not Fail (Joining the Thread)

This example demonstrates listening on a component instantiation (without a package) that does not fail and waiting for the thread to finish using `threadJoin`.

```coldfusion
var threadName=new Query():function(result,error) {
    thread.result=result;
};

// wait for the thread to finish
threadJoin(threadName);

dump(getMetadata(cfthread[threadName].result).fullname);
```

### Listening on a Component Instantiation (With Package) that Does Not Fail (Joining the Thread)

This example demonstrates listening on a component instantiation (with a package) that does not fail and waiting for the thread to finish using `threadJoin`.

```coldfusion
var threadName=new org.lucee.cfml.Query():function(result,error) {
    thread.result=result;
};

// wait for the thread to finish
threadJoin(threadName);

dump(getMetadata(cfthread[threadName].result).fullname);
```

### Listening on a Static Component Function (No Package) that Does Not Fail (Joining the Thread)

This example demonstrates listening on a static component function (without a package) that does not fail and waiting for the thread to finish using `threadJoin`.

```coldfusion
var threadName=Query::new(["columnName"]):function(result,error) {
    thread.result=result;
};

// wait for the thread to finish
threadJoin(threadName);

dump(cfthread[threadName].result.columnlist);
```

### Listening on a Static Component Function (With Package) that Does Not Fail (Joining the Thread)

This example demonstrates listening on a static component function (with a package) that does not fail and waiting for the thread to finish using `threadJoin`.

```coldfusion
var threadName=org.lucee.cfml.Query::new(["columnName"]):function(result,error) {
    thread.result=result;
};

// wait for the thread to finish
threadJoin(threadName);

dump(cfthread[threadName].result.columnlist);
```

### Listening on a UDF (Joining the Thread), Sending Data to a Function Collection; Test Success

This example demonstrates listening on a UDF, sending data to a function collection, and testing for success.

```coldfusion
function mySuccess() {
    return "Susi Sorglos";
}

coll1={
    onSuccess:function(result) {
        thread.success=result;
    }
    ,onFail:function(result,error) {
        thread.fail=error.message;
    }
};

var threadName1=mySuccess():coll1;

// wait for the thread to finish
threadJoin(threadName1);
dump(cfthread[threadName1].success);
```

### Listening on a UDF (Joining the Thread), Sending Data to a Function Collection; Test Fail

This example demonstrates listening on a UDF, sending data to a function collection, and testing for failure.

```coldfusion
function myError() {
    throw "Upsi dupsi!";
}

var threadName2=myError():{
    onSuccess:function(result) {
        thread.success=result;
    }
    ,onFail:function(result,error) {
        thread.fail=error.message;
    }
};

// wait for the thread to finish
threadJoin(threadName2);
dump(cfthread[threadName2].fail);
```

### Listening on a UDF (Joining the Thread), Sending Data to a Component; Test Success

This example demonstrates listening on a UDF, sending data to a component, and testing for success.

```coldfusion
function mySuccess() {
    return "Susi Sorglos";
}

var cfc=createObject("component","functionListener/Test.cfc");
var threadName1=mySuccess():cfc;

// wait for the thread to finish
threadJoin(threadName1);
dump(cfthread[threadName1].success);
```

### Listening on a UDF (Joining the Thread), Sending Data to a Component; Test Fail

This example demonstrates listening on a UDF, sending data to a component, and testing for failure.

```coldfusion
function myError() {
    throw "Upsi dupsi!";
}

var cfc=createObject("component","functionListener/Test.cfc");
var threadName2=myError():cfc;

// wait for the thread to finish
threadJoin(threadName2);
dump(cfthread[threadName2].fail);
```

### Async Execution Without a Listener; Null

This example demonstrates async execution without a listener, passing `null` as the listener.

```coldfusion
function logAndFail(name,value) {
    request.testFunctionListenerEcho[name]=value;
    throw "Upsi dupsi!";
}

var threadName1=logAndFail("testNull","Peter Lustig"):nullValue();

// wait for the thread to finish
threadJoin(threadName1);
dump(request.testFunctionListenerEcho.testNull);
```

### Async Execution Without a Listener; Empty Struct

This example demonstrates async execution without a listener, passing an empty struct as the listener.

```coldfusion
function logAndFail(name,value) {
    request.testFunctionListenerEcho[name]=value;
    throw "Upsi dupsi!";
}

var threadName2=logAndFail("testStruct","Ruedi Zraggen"):{};

// wait for the thread to finish
threadJoin(threadName2);
dump(request.testFunctionListenerEcho.testStruct);
```