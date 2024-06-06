<!--
{
  "title": "Exception - Cause",
  "id": "exception-cause",
  "since": "6.1",
  "keywords": [
    "exception",
    "error",
    "cause",
    "thread",
    "parent"
  ]
}
-->
# Exception - Cause

Lucee 6.1 improves its support for exception causes, providing better debugging and error handling capabilities.

## Tag Attribute cause

The `<cfthrow>` tag now includes a new attribute, `cause`, which allows you to add a cause to a newly created exception.

```run
<cfscript>
try {
    try {
        throw "Upsi dupsi!";
    }
    catch(e) {
        cfthrow (message="Upsi daisy!", cause=e);
    }
}
catch(ex) {
    dump(ex.message);
    dump(ex.cause.message);
}
</cfscript>
```

Thanks to this enhancement, you get not only the tag context and Java stack trace from the top-level exception, but also the same information for the "cause" exception.


## Parent Thread Context

When you throw an exception from a child thread, for example, a `cfhttp` call executed in parallel or an exception inside the `cfthread` tag, you can now see the stack trace from where that thread was started. Previously, you only saw the stack trace within the child thread. With Lucee 6.1, you also get the information from the parent thread as the cause. Consider the following example:

```run
<cfscript>
thread name="testexception" {
    throw "Upsi dupsi!"
}
threadJoin("testexception");
dump(cfthread["testexception"].error.message);
dump(cfthread["testexception"].error.cause.Message);
</cfscript>
```

The error not only includes the exception information from within the cfthread tag but also provides information from outside, making debugging much easier as you can see where the tag was called from.

