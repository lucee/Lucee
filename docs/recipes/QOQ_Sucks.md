<!--
{
  "title": "Query of Queries sometimes it rocks, sometimes it sucks",
  "id": "QOQ_Sucks",
  "related": [
    "tag-query",
    "function-queryexecute",
    "function-queryfilter"
  ],
  "categories": [
    "query"
  ],
  "description": "This document explains why Query of Queries (QoQ) may or may not be the best approach for your use case.",
  "keywords": [
    "Query of Queries",
    "QoQ",
    "Performance",
    "Query Filter",
    "Query Sort",
    "Lucee"
  ]
}
-->
## The good, the bad and the ugly ##

This document explains why Query of Queries (QoQ) may or may not be the best approach for your use case.

- **PRO**: It's nice to work with in-memory datasets/queries using SQL.
- **CON**: It can be very slow, depending on the use case.

Update: The performance of QoQ has been dramatically improved for single tables since 5.3.8!

[Improving Lucee's Query of Query Support](http://wwvv.codersrevolution.com/blog/improving-lucees-query-of-query-support)

There has also been a lot of work done to improve the "correctness" of the native SQL engine's behavior.

[QoQ tickets](https://luceeserver.atlassian.net/issues/?jql=text%20~%20%22qoq%22%20ORDER%20BY%20updated)

Currently, Lucee QoQ only supports `SELECT` statements; `UPDATE` and `INSERT` aren't yet supported.

Lucee has two QoQ engines, a fast native engine which only works on a single table.

Any SQL using multiple tables, i.e., with a JOIN, will fall back to the HSQLDB engine.

The HSQLDB engine requires loading all the queries into temporary tables and is currently Java synchronized, all of which can affect performance.

If the native QoQ engine fails on a single table query, by default, Lucee will attempt to fall back on the HSQLDB engine.

See `LUCEE_QOQ_HSQLDB_DISABLE` and `LUCEE_QOQ_HSQLDB_DEBUG` under [[running-lucee-system-properties]].

### Example: ###

```lucee+trycf
<cfscript>
	q = QueryNew("name, description");
	loop times=3 {
		getFunctionList().each(function(f){
			var fd = getFunctionData(arguments.f);
			var r = QueryAddRow(q);
			QuerySetCell(q,"name", fd.name, r);
			QuerySetCell(q,"description", fd.description, r);
		});
	}
	dump(server.lucee.version);
	dump(var=q.recordcount,
	    label="demo data set size");
	s = "the";
</cfscript>

<cftimer type="outline">
	<cfscript>
		q3 = q.filter(function(row){
			return (row.description contains s);
		});
	</cfscript>
</cftimer>
<cfdump var=#q3.recordcount#>
```

In this example, we have a QOQ with the persons table.

```luceescript
// index.cfm

directory sort="name" action="list" directory=getDirectoryFromPath(getCurrentTemplatePath()) filter="example.cfm" name="dir";
loop query=dir {
	echo('<a href="#dir.name#">#dir.name#</a><br>');
}
```

```luceescript
// example.cfm

max=1000;
persons=query(
	"lastname":["Lebowski","Lebowski","Lebowski","Sobchak"],
	"firstname":["Jeffrey","Bunny","Maude","Walter"]
	);

// Query of Query
start=getTickCount("micro");
loop times=max {
	query dbtype="query" name="qoq" {echo("
		select * from persons
		where lastname='Lebowski'
		and firstname='Bunny'
		order by lastname
	");}
}
dump("Query of Query Execution Time:"&(getTickCount("micro")-start));

// Query Filter/Sort
start=getTickCount("micro");
loop times=max {
	qf=queryFilter(persons,function (row,cr,qry) {return row.firstname=='Bunny' && row.lastname=='Lebowski';});
	qs=querySort(qf,"lastname");
}
dump("Query Filter/Sort Execution Time:"&(getTickCount("micro")-start));
```

In this example, we have two different methods of queries.

1) First one is QoQ. Here, `QoQ` from the `persons` table is executed a thousand times due to the looping required by QoQ.

2) The second one is calling the function query filter. Query filter filters out the same row the same way the QoQ does.

3) Execute it in the browser and we get two results (Query of Query execution time and Query filter/sort execution time). Query filter executes at least twice as fast as query of query. Because QoQ loops over and over again, it is slower. If you can avoid QoQ and use the Query filter/sort, your code will execute much faster.

### Footnotes ###

You can see these details in the video here:

[Query of Query Sucks](https://www.youtube.com/watch?v=bUBXzo1WbSM)