<!--
{
  "title": "Event Gateways in Lucee",
  "id": "event-gateways-lucee",
  "related": [
    "function-sendgatewaymessage"
  ],
  "categories": [
    "gateways"
  ],
  "description": "EG's are another way how to communicate with your Lucee server and are kind of a service running on Lucee, reacting on certain events.",
  "keywords": [
    "Event Gateway",
    "Custom Gateway",
    "SMS",
    "File Change",
    "Mail Server",
    "Slack Notification",
    "Lucee"
  ]
}
-->
## Lucee Event Gateways

First of all, it is necessary to explain how Event Gateways (EG) are working in the first place. EG's are another way to communicate with your Lucee server and are kind of a service running on Lucee, reacting to certain events. These kinds of events could be something along the lines of:

* SMS sent to a certain receiver
* File change happening in a directory
* Mail received on a mail server
* Slack notification received

What then can be done with these events is to trigger some actions that react to these events. For instance, if an SMS is sent to the server asking for the current heap memory space, the server could respond with an SMS returning the details. So you basically have an event producer and an event consumer.

Event Gateways have for a long time lived a quiet life in CFML for several reasons. The main reasons were the lack of diversity and implementations, which were due to the fact that EG's had to be written in Java and not every CFML developer is very familiar with Java. Given this downside, it is understandable that there are such few available event gateways available.

## Lucee's approach

In Lucee, EG's can be written in CFML, and this is what this description is all about, which now makes it way more attractive to write the decisive parts with your favorite language. Some parts sometimes still need perhaps a Java library, but coding around that normally is not really a problem. Just use the according JAR solution available for the specific event (like SMS or others).

### What are the involved components in Lucee?

There are 2 components that are important for writing an event gateway:

* Gateway driver
* Event Gateway

The gateway driver is a CFC that is always instantiated and running. It is responsible for managing the lifecycle of the event gateway. The event gateway is the actual implementation of the event handling logic.

### Testing the Event Gateway

I have created a template called testGateway.cfm and use the following code to test the result.

```lucee
<cfset sMessage = "something I need to log.">
<cfset sendGatewayMessage("logMe", {})>
```

<img src="/uploads/default/original/1X/a127ee6bf8b4df77c6956ba2cada99ab4642e7ff.jpg" width="593" height="163">

Now the sanity checks kick in and prevent faulty data from being sent to the Gateway. So once we change the code to this:

```lucee
<cfset sMessage = "something I need to log.">
<cfset sendGatewayMessage("logMe", {message:sMessage, type:"error"})>
```

We receive the expected blank page. In the background, the message has been passed to the Gateway through the sendGateway() method and the data will be written by the start() endless loop into the logfile with the help of the method _log().

How you actually write your EG is totally up to you. But now, do it in CFML!

## Further examples for Event Gateway implementations

Above we have introduced the possibility to asynchronously log some data to a log file. There are additional other Event Gateways you can think of or use:

* ICQ watcher
* Slack Channel inspector
* Listen to a socket
* On incoming email

The possibilities are huge and we expect several new event gateways to emerge in the next few months. Have fun with Lucee.