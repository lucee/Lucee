<!--
{
  "title": "Lazy Queries",
  "id": "lazy_queries",
  "categories": [
    "query"
  ],
  "description": "How to use lazy queries",
  "keywords": [
    "Lazy Queries",
    "Regular Queries",
    "Performance",
    "Memory Optimization",
    "Lucee"
  ]
}
-->
## Lazy Queries ##

This document explains about lazy queries with some simple examples as follows:

### Example 1: ###

**Regular query**: Regular query tags/functions load all data inside a two-dimensional structure to use.

```luceescript
//regularQuery.cfm
query name="qry" returntype="query" {
	echo("select * from lazyQuery");
}
dump(numberFormat(qry.getColumnCount()*qry.getRowCount()));
loop query=qry {
	dump(qry.val);
	if(qry.currentrow==10) break;
}
```

1) In this example, we have a simple task. All statements return a result set with 200,000 records. We output the first ten. Then we make a break when we add ten rows.

2) We execute this in the browser and we get the expected result.

### Example 2: ###

**Lazy query**: Lazy queries keep a pointer to the database and only load the data on demand. If you loop through a query, the data is loaded on the spot. It does not create a two-dimensional struct to store all the data beforehand. When the query tag is done, it keeps a pointer to the database.

As a lazy query loops through, it loads the data on demand so you do not have to wait until it has loaded all the data. It is faster when you only load the first 10 rows as you don't have to wait until it's done loading everything.

```luceescript
//lazyQuery.cfm
query name="qry" returntype="query" lazy=true {
	echo("select * from lazyQuery");
}
loop query=qry {
	dump(qry.val);
	if(qry.currentrow==10) break;
}
```

This example is similar to a regular query, but we define `lazy=true`. So that Lucee knows to do a lazy query.

I have removed the column count from the example. Record count is no longer possible because it does not read all the data initially. It does not know how many records there are. When you loop, you can count the records, so you know the total number of records at the end, but not at the start.

There is not really a difference between a regular query and a lazy query, just some limitations (you cannot get the record count in the beginning, and you cannot use cache) within a lazy query.

With a lazy query, we do not have to wait until Lucee has loaded all the data into a two-dimensional structure, and it is also better for memory because you do not have to store all the older data in the memory until you are ready to use it. So there are some benefits.

### Example 3: ###

A comparison of lazy queries and regular queries follows:

```luceescript
types=['regular':false,'lazy':true];
results=structNew("ordered");
loop struct=types index="type" item="lazy" {
	loop from=1 to=10 index="i" {
		start=getTickCount('nano');
		query name="qry" returntype="query" lazy=lazy {
			echo("select * from lazyQuery");
		}
		x=qry.val;
		time=getTickCount('nano')-start;

		if(isNull(results[type]) || results[type]>time)results[type]=time;
	}
}
// format results
results.regular=decimalFormat(results.regular/1000000)&"ms";
results.lazy=decimalFormat(results.lazy/1000000)&"ms";
dump(results);
```

This example compares lazy queries with regular queries. It has a loop that loops two times: once for a regular query and a second one for a lazy query. The `type` is used here with `lazy=lazy`, So it sets true or false and does that ten times, once for every time the loops execute. It stores the execution time but you only get the fastest execution time of the ten tries.

Execute that example in the browser. The regular query takes 41 milliseconds and the lazy query takes 27 milliseconds. So we see the benefits of the lazy queries.

### Footnotes ###

You can see the details in this video:
[Lazy Query](https://youtu.be/X8_TB1py8n0)