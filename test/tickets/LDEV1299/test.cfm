<cfscript>
qry = queryNew("id,name,mail,showtime", "integer,varchar,varchar,timestamp", [
    [1, "pothy", "pothy@test.com", now()],
    [2, "mitrah", "mitrah@test.com", now()],
    [3, "soft", "soft@test.com", now()]
]);

cacheAfterThis = dateAdd("s", 2, now());

qryToCache = queryExecute(
    "SELECT * FROM qry",
    [],
    {dbtype="query", qry=qry, cachedAfter=cacheAfterThis}
);

sleep(5000);

qryFrmCache = queryExecute(
    "SELECT * FROM qry",
    [],
    {dbtype="query", qry=qry, cachedAfter=cacheAfterThis}
);

writeOutput(qryFrmCache.RECORDCOUNT);
</cfscript>