<!--
{
  "title": "Adding Caches via Application.cfc",
  "id": "cookbook-caches-in-application-cfc",
  "related": [
    "tag-application"
  ],
  "categories": [
    "application",
    "cache"
  ],
  "menuTitle": "Adding Caches",
  "description": "How to add per-application caches via Application.cfc in Lucee.",
  "keywords": [
    "Caches",
    "Application.cfc",
    "Per-application caches",
    "Cache connections",
    "Default caches",
    "Lucee"
  ]
}
-->
## Adding Caches via Application.cfc

It is possible to add cache connections in Lucee 5.1+ on a per-application basis by adding configuration to your `Application.cfc`. You can also select the default object cache, query cache, function cache, etc. Note if these caches use an extension that provides the cache driver, the extension must be installed already.

To declare cache connections, create a struct called `this.cache.connections` in the pseudo constructor of your `Application.cfc`. Each key in the struct will be the name of the cache connection to create, and the value of the item will be another struct defining the properties of that cache connection.

```lucee
this.cache.connections["myCache"] = {
    class: 'org.lucee.extension.cache.eh.EHCache',
    bundleName: 'ehcache.extension',
    bundleVersion: '2.10.0.25',
    storage: false,
    custom: {
        "bootstrapAsynchronously":"true",
        "replicatePuts":"true",
        etc...
    },
    default: 'object'
};
```

Note, there is a shortcut for `this.cache.connections["myCache"] = {}` and that is `this.cache["myCache"] = {}`. We support both since the latter is closer to how datasources are defined.

### Generating Cache Connection code

The easiest way to generate the code block above is to follow these steps:

1. Start up a Lucee server
2. Create the cache you want via the web admin
3. Edit the cache and scroll to the bottom
4. Copy the code snippet that appears directly into your `Application.cfc`

### Cache metadata

Let's take a look at some of the keys used to define a cache connection.

* **class** - This is the Java class of the driver for the cache engine.
* **bundleName** - Optional. The name of the OSGI bundle to load the `class` from.
* **bundleVersion** - Optional. The version of the OSGI bundle to load the `class` from.
* **storage** - A boolean that flags whether this cache can be used for client or session storage.
* **custom** - A struct of key/value pairs for configuring the cache. This struct is entirely dependent on the cache driver in use, so refer to the docs for that cache driver to see the possible values. Note, some of these custom values might be required for some cache drivers to work.
* **default** - Optional. If you want this cache to be used as a default cache, then give this one of these values: `function`, `object`, `template`, `query`, `resource`, `include`, `http`, `file`, `webservice`.

### Default Caches

When declaring a cache, you can make it the default cache for creation operations, but it is also possible to configure the default caches for each operation all at once in your `Application.cfc` like so:
```lucee
this.cache.object = "myCache";
this.cache.template = "AnotherCache";
this.cache.query = "yetAnother";
this.cache.resource = "<cache-name>";
this.cache.function = "<cache-name>";
this.cache.include = "<cache-name>";
this.cache.http = "<cache-name>";
this.cache.file = "<cache-name>";
this.cache.webservice = "<cache-name>";
```

A single cache can only be the default storage location for a single operation at a time. For example, a cache named "myCache" cannot both be the default cache for objects as well as queries.