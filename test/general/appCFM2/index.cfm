<cfscript>
	cachePut(id:'a', value:'1',timeSpan:CreateTimeSpan(0,0,0,1),cacheName:'object');
	echo(cacheGet(id:'a',cacheName:'object'));

	cachePut(id:'b', value:'2',timeSpan:CreateTimeSpan(0,0,0,1));
	echo(cacheGet(id:'b'));
</cfscript>