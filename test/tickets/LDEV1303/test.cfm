<cfscript>
	cachedVar = cacheGet("testEHcache");
	cachedVar = APPLICATION.ApplicationName;
	writeOutput(cachePut("testEHcache", cachedVar));
</cfscript>