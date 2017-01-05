<cfscript>
	cachedVar = cacheGet("test");
	if (isNull(cachedVar)) {
		cachedVar = APPLICATION.ApplicationName;
		cachePut("test", cachedVar);
	}
	writeOutput(cachedVar);
</cfscript>