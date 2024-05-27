<!--
{
  "title": "Monitoring/Debugging",
  "id": "monitoring-debugging",
  "since": "6.1",
  "description": "Learn about the changes in Lucee 6.1 regarding Monitoring and Debugging. Understand the old and new behavior, and how to configure the settings in Lucee Admin and Application.cfc.",
  "keywords": [
    "monitoring",
    "debugging",
    "admin",
    "Application.cfc",
    "cfsetting",
    "debug",
    "showdebugoutput",
    "cfapplication"
  ]
}
-->
# Monitoring/Debugging

Lucee 6.1 changed how you handle Monitoring/Debugging.

## Old Behaviour
Previously, you could enable/disable Debugging in the Lucee admin, and enable/disable specific debug options like showing template execution. With the tag `<cfsetting showDebugOutput="true|false">`, you could define whether the debugging is shown or not.

## New Behaviour
Lucee 6 has completely overhauled this functionality. Instead of having "Metrics" and "Reference" as part of the "Modern" Debug Template, they are now independently controlled under the new umbrella term "Monitoring".

### Lucee Admin
The "debugging" settings are now under the group "Monitoring" and there is a new page called "Output".

#### Page "Output"
On the "Output" page, you define which sections of monitoring are shown:
- Debugging
- Metrics
- Documentation (formerly "Reference")
- Test (available soon)

This is similar to the action `<cfsetting showOutput="true|false">` (more on that tag later).

#### Page "Settings"
You no longer enable/disable debug as a whole, only the options. If no options are enabled, debugging is disabled. Thus, the general switch was/is not really needed anymore.

#### Page "Debug Templates"
Here you can choose the debug template you want to use. You can limit it to a specific IP Range if you like, and you can also define different templates for different IP Ranges, though one template can cover all requests.

#### Page "Logs"
This page allows you to show the last X requests (depending on your settings), which is useful if no debugging is shown on the output.

### Application.cfc
Lucee 6.1 now allows you to overwrite all these settings in the Application.cfc, which was not possible in previous versions.

You can define what is shown:
```lucee
this.monitoring.showDebug = true;
this.monitoring.showDoc = true;
this.monitoring.showMetric = true;
this.monitoring.showTest = true; // following soon
```

And also enable/disable debug options:
```lucee
this.monitoring.debuggingTemplate = true;
this.monitoring.debuggingDatabase = true;
this.monitoring.debuggingException = true;
this.monitoring.debuggingTracing = true;
this.monitoring.debuggingDump = true;
this.monitoring.debuggingTimer = true;
this.monitoring.debuggingImplicitAccess = true;
this.monitoring.debuggingThread = true;
```

You can also export all these settings in the Lucee Administrator on the Monitoring/Settings page.

### In Your Code
Even after the Application.cfc, you can still change these settings.

With the help of the tag `<cfapplication>`:
```lucee
<cfapplication 
    action="update" 
    showDebug="false"
    showDoc="true"
    showMetric="false"
    showTest="false"
    debuggingTemplate="false"
    debuggingDatabase="true">
```

Or with the tag `<cfsetting>` you can change the "show" settings (not the debug options):
```lucee
<cfsetting 
    showDebug="false"
    showDoc="true"
    showMetric="false"
    showTest="false">
```

#### Downside/Upside
Of course, when you enable, for example, "debuggingTemplate" in your code, everything that happened before was not logged and is lost. But this can also be a benefit, as it allows you to do things like this:
```lucee
try {
    application action="update" debuggingTemplate=false;
    include "mysecretcode.cfm";
} finally {
    application action="update" debuggingTemplate=true;
}
```

This way, you can prevent Lucee from logging certain code.

## Tab Documentation (formerly Reference)
This tab now not only gives you a function and tag reference, it also provides all kinds of "recipes" like this.

## Backward Compatibility
These new features are fully backward compatible.

### Tag cfsetting
The old attribute "showDebugOutput" is now an alias to the newly introduced "show" attribute. This means with this attribute you can still enable/disable Monitoring as a whole.

So when you do:
```lucee
<cfsetting showDebugOutput="false">
```
It will not show the monitoring at all, the same way as you would do:
```lucee
<cfsetting show="false">
```

## Conclusion
Lucee 6.1 gives you full control over Monitoring in your code, making it easier for every developer to use it.