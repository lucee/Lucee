<!--
{
  "title": "List existing Cache Connections",
  "id": "cookbook-cache-list",
  "categories": [
    "cache"
  ],
  "description": "List existing Cache Connections available in Lucee.",
  "keywords": [
    "Cache",
    "Cache connections",
    "List cache",
    "hasCache",
    "cacheNames",
    "Lucee"
  ]
}
-->
## List existing Cache Connections

There is now a built-in function in Lucee to list existing cache connections, but you can also easily do this using the following:

```cfs
/**
* returns all available cache names as an array
*/
array function cacheNames() {
    return getPageContext().getConfig().getCacheConnections().keySet().toArray();
}
```

This function returns an array containing all cache connections available.

```cfs
/**
* checks if a cache with the given name is defined
* @cacheName name of the cache to look for
*/
boolean function hasCache(required string cacheName) {
    var it = getPageContext().getConfig().getCacheConnections().keySet().iterator();
    loop collection="#it#" item="local.name" {
        if (cacheName.trim() == name) return true;
    }
    return false;
}
```

This is a variation of this function that checks if a cache with the given name exists.