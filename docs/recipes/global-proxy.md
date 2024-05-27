<!--
{
  "title": "Global Proxy",
  "id": "global-proxy",
  "since": "6.0",
  "description": "Learn how to define a global proxy in Lucee. This guide demonstrates how to set up a global proxy in the Application.cfc file, limit the proxy to specific hosts, and exclude specific hosts from using the proxy.",
  "keywords": [
    "CFML",
    "proxy",
    "global proxy",
    "Lucee",
    "Application.cfc"
  ]
}
-->
# Global Proxy

Since version 6.0, Lucee allows you to define a global proxy in the Application.cfc.

If set, it will affect all connections made to the "outside world".

```lucee
this.proxy = {
    server: "myproxy.com",
    port: 1234,
    username: "susi",
    password: "sorglos"
};
```

## Include

You can also limit the proxy to specific hosts by defining a list (or array) of hosts it should be used for:

```lucee
this.proxy = {
    server: "myproxy.com",
    port: 1234,
    username: "susi",
    password: "sorglos",
    includes: "whatever.com,lucee.org"
};
```

## Exclude

Or you can do the opposite, defining for which hosts it should not apply:

```lucee
this.proxy = {
    server: "myproxy.com",
    port: 1234,
    username: "susi",
    password: "sorglos",
    excludes: ["lucee.org", "whatever.com"]
};
```