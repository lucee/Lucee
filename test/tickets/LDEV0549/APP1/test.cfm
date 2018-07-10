<cfscript>
	cachedVar = cacheGet("default");
	cachedVar = APPLICATION.ApplicationName;
	cachePut("default", cachedVar);
	writeOutput(cachedVar);
</cfscript>