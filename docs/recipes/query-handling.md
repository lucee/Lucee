<!--
{
  "title": "Query Handling In Lucee",
  "id": "query-handling-lucee",
  "related": [
    "tag-query",
    "tag-queryparam",
    "function-queryexecute"
  ],
  "categories": [
    "query"
  ],
  "description": "How to do SQL Queries with Lucee.",
  "keywords": [
    "Query",
    "SQL",
    "QueryParam",
    "Query Builder",
    "cfquery",
    "params",
    "Query Handling"
  ]
}
-->
This document explains how SQL queries are supported in Lucee.

## Query tags

[tag-query] different ways to use the tags in Lucee and how we can pass the value into the query.

```lucee
<cfquery name="qry" datasource="test">
    select * from Foo1890
</cfquery>
<cfdump var="#qry#" expand="false">
```

The above example just shows how to retrieve the data from the database.

### Using QueryParam

The [tag-QueryParam] is used inside the [tag-query] tag. It is used to bind the value with the SQL statement.

```lucee
<cfquery name="qry" datasource="test">
    select * from Foo1890
    where title=<cfqueryparam sqltype="varchar" value="Susi">
</cfquery>
<cfdump var="#qry#" expand="false">
```

Passing values with [tag-QueryParam] has two advantages:

* The value you pass in QueryParam is very secure.
* Lucee is able to cache the query statement and reuse it as long as the value is unchanged.

### Params

Here we use params as part of [tag-cfquery] tag, used to pass the value with SQL statement.

Pass the params value with struct.

```lucee
<cfquery name="qry" datasource="test" params="# {'title': 'Susi'} #">
    select * from Foo1890
    where title=:title
</cfquery>
<cfdump var="#qry#" expand="false">
```

Referenced as `:key` in SQL.

The below example shows how to pass more information using a struct.

```lucee
<cfquery name="qry" datasource="test" params="# {'title': {type: 'varchar', value: 'Susi'}} #">
    select * from Foo1890
    where title=:title
</cfquery>
<cfdump var="#qry#" expand="false">
```

You can pass the params value using an array. It is referenced as `?` in SQL.

```lucee
<cfquery name="qry" datasource="test" params="# ['Susi'] #">
    select * from Foo1890
    where title=?
</cfquery>
<cfdump var="#qry#" expand="false">
```

### Query Builder

Query Builder is used as an extension; it will not come up with core.

It is much easier to do a simple query.

```lucee
<cfscript>
// Query Builder (creates SQL in dialect based on the datasource defined)
qb = new QueryBuilder("test")
    .select("lastName")
    .from("person")
    .where(QB::eq("firstname", "Susi"));
qb.execute();
dump(res);
</cfscript>
```

Use `QueryBuilder("test")` as constructor.

* Define a datasource with constructor or `setDatasource('test')` function.
* Use `select("lastName")` to select the column.
* Use `from("person")` from which table you want to retrieve data.
* Where statement like `where(QB::eq("firstname", "Susi"))`.

Use `qb.execute()` to obtain the result.

You can change the selected column like in the example below.

```lucee
<cfscript>
// change select
qb.select(["age", "firstname"]);
qb.execute();
dump(res);
</cfscript>
```

You can also change the where condition as shown in the example below.

```lucee
<cfscript>
// change where
qb.clear("where");
qb.where(
    QB::and(
        QB::eq("firstname", "Susi"),
        QB::neq("lastname", "Moser"),
        QB::lt("age", 18)
    )
);
qb.execute();
dump(res);
</cfscript>
```

### Footnotes

Here you can see the above details in a video:

[https://www.youtube.com/watch?time_continue=684&v=IMdPM58guUQ](https://www.youtube.com/watch?time_continue=684&v=IMdPM58guUQ)