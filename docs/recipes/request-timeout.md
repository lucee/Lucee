<!--
{
  "title": "Request Timeout",
  "id": "request-timeout",
  "description": "Learn how to use request timeout correctly with Lucee.",
  "keywords": [
    "request timeout",
    "timeout",
    "memory",
    "cpu",
    "Concurrent Requests",
    "Administrator",
    "Application.cfc",
    "cfsetting",
    "Threshold"
  ]
}
-->

# Request Timeout

Lucee allows you to define a request timeout for every request made to Lucee. **Never accept request timeouts as a regular behavior of your application; always try to resolve any request timeout that occurs.**

## Setting Request Timeout

### Lucee Administrator
You can set the request timeout in the Lucee Administrator under "Settings/Request".

### Application.cfc
You can also set the request timeout in the `Application.cfc` as follows:

```luceescript
this.requestTimeout = createTimeSpan(0, 0, 0, 49);
```

### Tag cfsetting
Alternatively, you can set the request timeout using the `<cfsetting>` tag:

```luceetag
<cfsetting requestTimeout="#createTimeSpan(0, 0, 0, 49)#">
```

## Thresholds

Lucee includes several additional thresholds that requests must meet before they are terminated due to a timeout. These thresholds help prevent unnecessary termination of requests, which can pose risks such as deadlocks and open monitors. If a request timeout is reached but the thresholds are not met, Lucee will log the event in the "requesttimeout" log instead of terminating the request.

### Concurrent Requests Threshold
This setting allows you to specify the number of concurrent requests Lucee can handle before enforcing request timeouts. Adjusting this threshold can help manage request timeouts under varying loads. A higher threshold allows more concurrent requests to be processed without enforcing timeouts, potentially improving performance under heavy loads at the risk of longer request times. The default threshold is set to 0, meaning request timeouts are enforced immediately for all requests. 

Set this threshold via the System Property:
```sh
-Dlucee.requesttimeout.concurrentrequestthreshold=100
```
or the Environment Variable:
```sh
LUCEE_REQUESTTIMEOUT_CONCURRENTREQUESTTHRESHOLD=100
```

### CPU Usage Threshold
This option allows you to set a CPU usage threshold that Lucee monitors before enforcing request timeouts. The threshold value is a float ranging from 0.0 (0% CPU usage) to 1.0 (100% CPU usage). When the system's CPU usage is below this threshold, Lucee processes requests without applying the request timeout rule. This helps manage resource allocation and maintain application responsiveness during high demand or limited system resources. The default setting is 0.0, which means request timeouts are applied regardless of CPU usage.

Set this threshold via the System Property:
```sh
-Dlucee.requesttimeout.cputhreshold=0.9
```
or the Environment Variable:
```sh
LUCEE_REQUESTTIMEOUT_CPUTHRESHOLD=0.9
```

### Memory Usage Threshold
This setting allows you to establish a memory usage threshold, guiding Lucee on when to enforce request timeouts based on current memory consumption. The threshold value is a float from 0.0 (0% memory usage) to 1.0 (100% memory usage). By monitoring memory usage against this threshold, Lucee decides whether to enforce or relax request timeouts dynamically. This prevents system overloads and ensures stable performance by not strictly applying timeouts when memory usage is below the defined threshold. The default threshold is set to 0.0, meaning Lucee will apply request timeouts without considering memory usage.

Set this threshold via the System Property:
```sh
-Dlucee.requesttimeout.memorythreshold=0.8
```
or the Environment Variable:
```sh
LUCEE_REQUESTTIMEOUT_MEMORYTHRESHOLD=0.8
```
