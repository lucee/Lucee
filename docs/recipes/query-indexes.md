<!--
{
  "title": "Query Indexes",
  "id": "query-indexes",
  "since": "6.0",
  "description": "Learn how to set and use indexes for query results in Lucee. This guide demonstrates how to define a query with an index and access parts of the query using the index.",
  "keywords": [
    "query",
    "indexes",
    "cfquery"
  ]
}
-->
# Query Indexes

Since Lucee 6.0, you can set an index for a query result, which you can then use down the line.

## Setting an Index

This example shows how to define a query with an index:

```lucee
<cfquery name="qry" datasource="mysql" indexName="id">
    select 1 as id, 'Susi' as name
    union all
    select 2 as id, 'Peter' as name
</cfquery>
```

You can then access parts of the query like this:

```lucee
<cfdump var="#qry#">
<cfdump var="#QueryRowByIndex(qry, 2)#">
<cfdump var="#QueryRowDataByIndex(qry, 2)#">
<cfdump var="#QueryGetCellByIndex(qry, 'name', 2)#">
```