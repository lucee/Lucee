<!--
{
  "title": "Cache a query for the current request",
  "id": "cookbook-cached-within-request",
  "related": [
    "tag-query"
  ],
  "categories": [
    "cache",
    "query"
  ],
  "description": "Cache a Query for the current request in Lucee.",
  "keywords": [
    "Cache",
    "Query",
    "Request cache",
    "cachedWithin",
    "cfquery"
  ]
}
-->
## Cache a Query for the current request

Perhaps you're familiar with the "cachedwithin" attribute of the tag [tag-query], which is normally used as follows:

```coldfusion
<cfquery cachedWithin="#createTimeSpan(0,0,0,1)#">
  select * from whatever where whatsoever='#whatsoever#'
</cfquery>
```

This caches the query result for ALL users for one second. This is sometimes used to cache a query for the current request because usually most requests are completed in less than a second.

The problem is that this cache applies to all requests and that's more complicated for Lucee, meaning unnecessary overhead on the system.

Request query caching is a simple solution to this problem. Replace the timespan defined in the "cachedWithin" attribute with the value "request":

```coldfusion
<cfquery cachedWithin="request">
  select * from whatever where whatsoever='#whatsoever#'
</cfquery>
```

Then the query is cached for only the current request, independent of how long the request takes!