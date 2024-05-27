<!--
{
  "title": "Query of Queries (QoQ)",
  "id": "query-of-queries",
  "related": [
    "tag-query"
  ],
  "categories": [
    "query"
  ],
  "description": "Query of queries (QoQ) is a technique for re-querying an existing (in memory) query without another trip to the database.",
  "keywords": [
    "Query of Queries",
    "QoQ",
    "SQL",
    "In memory query",
    "Lucee",
    "dbtype query"
  ]
}
-->
## Introduction

Query of queries (QoQ) is a technique for re-querying an existing (in memory) query without another trip to the database. This allows you to dynamically combine queries from different databases.

```lucee
<!--- query a database in the normal manner --->
<cfquery name="sourceQry" datasource="mydsn">
  SELECT    *
  FROM   my_db_table
</cfquery>

<!--- query the above query *object*. (this doesn't make a call to the database.) --->
<cfquery name="newQry" dbtype="query"><!--- the dbtype="query" attribute/value enables QoQ --->
  SELECT    *
  FROM    sourceQry <!--- instead of a real database table name, use the variable name of the source query object --->
</cfquery>
```

The above example isn't very useful, because `newQry` is a straight copy of the source query, but it demonstrates the two requirements of `QoQ`:

* The dbtype="query" attribute
* A source query object name (e.g., sourceQry) instead of a table name in the FROM clause.

### Example: Filtering

Let's say you have the following database query, `myQuery`:

```lucee
<cfquery name="myQuery" datasource="mydsn">
  SELECT    Name, Age, Location
  FROM    People
</cfquery>
```

You would now have a list of names, ages, and locations for all the people in a query called `myQuery`.

Say you want to filter out people under 18 and over 90, but you don't want to hit the database again:

```lucee
<cfquery name="filteredQuery" dbtype="query">
  SELECT     Name, Age, Location
  FROM    myQuery
  WHERE    Age >= 18
           AND Age <= 90
</cfquery>
```

`filteredQry` contains the desired records.

### Internals

Lucee uses its own SQL implementation for QoQ; when that fails, HSQLDB is tried.

Lucee's SQL implementation is a basic subset of ANSI92, but it is relatively fast. [HSQLDB is a more complete SQL implementation](http://hsqldb.org/doc/2.0/guide/sqlgeneral-chapt.html), but it is slow compared to Lucee's implementation.

### Supported Constructs

Even though under the hood, Lucee handles the fallback to HSQLDB automatically, it still can be useful to know what's possible with the fast Lucee SQL implementation versus the slower, fallback HSQLDB SQL implementation.

### Lucee's SQL Implementation

**Keywords and Operators**

* <=
* <>
* =
* =>
* =
* !=
* ALL
* AND
* AS
* BETWEEN x AND y
* DESC/ASC
* DISTINCT
* FROM
* GROUP BY
* HAVING
* IN ()
* IS
* IS NOT NULL
* IS NULL
* LIKE
* NOT
* NOT IN ()
* NOT LIKE
* OR
* ORDER BY
* SELECT
* TOP
* UNION
* WHERE
* XOR

Functions

TODO: Flesh this out.

### HSQLDB SQL Implementation

This is the fallback for when Lucee's SQL implementation can't handle the QoQ syntax. See the [HSQLDB documentation](http://hsqldb.org/doc/2.0/guide/sqlgeneral-chapt.html) for details.

### Footnotes

Lucee Google Groups Post: SQL syntax supported by query-of-queries?