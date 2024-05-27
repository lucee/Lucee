<!--
{
  "title": "Timeout",
  "id": "timeout",
  "description": "Learn how to use the <cftimeout> tag in Lucee. This guide demonstrates how to define a timeout specific to a code block, handle timeouts with a listener, and handle errors within the timeout block.",
  "keywords": [
    "tag",
    "timeout",
    "listener",
    "Lucee",
    "cftimeout",
    "error handling"
  ]
}
-->
# Timeout

Since version 6.0, Lucee supports the tag `<cftimeout>`. This tag allows you to define a timeout specific to a code block.

## Basic Usage

This example shows how to define a timeout for a code block:

```lucee
<cftimeout timespan="#createTimespan(0, 0, 0, 0,100)#" forcestop=true ontimeout="#function(timespan) {
    dump(timespan);
}">
    <cfset sleep(1000)>
</cftimeout>
```

You define how long the code within the tag is allowed to run and a listener (closure) that is called in case the timeout is reached. In this case, the listener `onTimeout` simply dumps the timespan.

## Error Handling

You can also add an additional listener `onError` that is called in case an exception occurs within the timeout block. If you want to escalate the exception, you simply rethrow the exception like in the following example:

```lucee
<cftimeout timespan="0.1" 
    onerror="#function(cfcatch){
        dump(arguments);
        throw cfcatch;
    }#" 
    ontimeout="#function(timespan) {
        dump(timespan);
    }#">
    <cfthrow message="upsi dupsi">
</cftimeout>
```