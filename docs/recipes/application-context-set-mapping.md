<!--
{
  "title": "Event Handling in Application.cfc",
  "id": "cookbook-event-handling",
  "related": [
    "tag-application",
    "function-onrequest",
    "function-onerror",
    "function-oncfcrequest",
    "function-onabort",
    "function-ondebug",
    "function-onmissingtemplate"
  ],
  "categories": [
    "application",
    "event handling"
  ],
  "description": "An overview of event handling functions in Application.cfc for Lucee.",
  "images": {
    "before_method": "/assets/images/listeners/queryListenerBefore.png",
    "after_method_cfquery": "/assets/images/listeners/queryListenerAfter.png",
    "after_method_queryexecute": "/assets/images/listeners/queryListenerAfterNoResult.png",
    "query_result": "/assets/images/listeners/queryListenerResult.png"
  },
  "keywords": [
    "Event Handling",
    "Application.cfc",
    "onApplicationStart",
    "onSessionStart",
    "onRequestStart",
    "onRequest",
    "onCFCRequest",
    "onError",
    "onAbort",
    "onDebug",
    "onMissingTemplate"
  ]
}
-->
Lucee provides several event handling functions within `Application.cfc` that can be used to manage different stages and types of requests. Here is an overview of these functions and their usage.

### OnApplicationStart ###

This method is triggered when the application starts.

```cfs
component {
   void function onApplicationStart() {
       echo('Application started');
   }
}
```

### OnSessionStart ###

This method is triggered when a session starts.

```cfs
component {
   void function onSessionStart() {
       echo('Session started');
   }
}
```

### OnRequestStart ###

This method is triggered at the start of each request.

```cfs
component {
   void function onRequestStart(string targetPage) {
       echo('Request started: ' & targetPage);
   }
}
```

### OnRequest ###

This method handles the actual request. In Lucee, this function is called even if the target page does not exist physically or is never used.

```cfs
component {
   void function onRequest(string targetPage) {
       echo('<html><body>Hello World</body></html>');
   }
}
```

### OnCFCRequest ###

Similar to "onRequest", but this function is used to handle remote component calls (HTTP Webservices).

```cfs
component {
   void function onCFCRequest(string cfcName, string methodName, struct args) {
       echo('<html><body>Hello World</body></html>');
   }
}
```

### OnError ###

This method is triggered when an uncaught exception occurs in this application context.

```cfs
component {
   void function onError(struct exception, string eventName) {
       dump(var:exception, label:eventName);
   }
}
```

As arguments you receive the exception (cfcatch block) and the eventName.

### OnAbort ###

This method is triggered when a request is ended with help of the tag `<cfabort>`.

```cfs
component {
   void function onAbort(string targetPage) {
       dump('request ' & targetPage & ' ended with an abort!');
   }
}
```

### OnDebug ###

This method is triggered when debugging is enabled for this request.

```cfs
component {
   void function onDebug(struct debuggingData) {
       dump(var:debuggingData, label:'debug information');
   }
}
```

### OnMissingTemplate ###

This method is triggered when a requested page was not found and **no function "onRequest" is defined**.

```cfs
component {
   void function onMissingTemplate(string targetPage) {
       echo('missing:' & targetPage);
   }
}
```

## Application.cfc Default Template ##

Below you can find an Application.cfc template that may serve as a starting point for your own applications settings with Lucee CFML engine. 

When creating an Application.cfc for the first time, you can configure all the settings within the Lucee Server or Web Administrator and use its "Export" tool (Lucee Administrator => Settings => Export) to move (by copy and paste) the settings into your Application.cfc file.

```cfs
component {

    /**
    * @hint onApplicationStart() is triggered when the application starts.
    */
    public boolean function onApplicationStart(){

        return true;

    }


    /**
    * @hint onSessionStart() is triggered when a session starts.
    */
    public boolean function onSessionStart(){

        return true;

    }


    /**
    * @hint onRequestStart() is triggered at the start of each request.
    */
    public boolean function onRequestStart(string targetPage){

        return true;

    }


    /**
    * @hint onRequest() is triggered during a request right after onRequestStart() ends and before onRequestEnd() starts. Unlike other CFML engines, Lucee executes this function without looking for the "targetPage" defined, while other CFML engines will complain if the targetPage doesn’t physically exist (even if not used in the onRequest() function).
    */
    public void function onRequest(string targetPage){

        include arguments.targetPage;
        return;

    }


    /**
    * @hint onRequestEnd() is triggered at the end of a request, right after onRequest() finishes.
    */
    public void function onRequestEnd(){

        return;

    }


    /**
    * @hint onCFCRequest() is triggered during a request for a .cfc component, typically used to handle remote component calls (e.g. HTTP Webservices).
    */
    public void function onCFCRequest(string cfcName, string methodName, struct args){

        return;

    }


    /**
    * @hint onError() is triggered when an uncaught exception occurs in this application context.
    */
    public void function onError(struct exception, string eventName){

        return;

    }


    /**
    * @hint OnAbort() is triggered when a request is ended with help of the "abort" tag.
    */
    public void function onAbort(string targetPage){

        return;

    }


    /**
    * @hint onDebug() is triggered when debugging is enabled for this request.
    */
    public void function onDebug(struct debuggingData){

        return;

    }


    /**
    * @hint onMissingTemplate() is triggered when the requested page wasn’t found and no "onRequest()" function is defined.
    */
    public void function onMissingTemplate(string targetPage){

        return;

    }

}
```