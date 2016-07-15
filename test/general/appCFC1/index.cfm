<cfscript>
	cachePut(id:'abc', value:'123',timeSpan:CreateTimeSpan(0,0,0,1),cacheName:'susi');
	echo(cacheGet(id:'abc',cacheName:'susi'));

	cachePut(id:'def', value:'456',timeSpan:CreateTimeSpan(0,0,0,1));
	echo(cacheGet(id:'def'));
</cfscript>