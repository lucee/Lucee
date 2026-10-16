<cfscript>
	child         = new child.Default();
	meta          = getMetadata( child );
	functionNames = [];
	renderCacheable = "";

	for ( func in ( meta.functions ?: [] ) ) {
		arrayAppend( functionNames, lCase( func.name ) );
		if ( lCase( func.name ) == "render" ) {
			renderCacheable = func.cacheable ?: "";
		}
	}

	echo( serializeJSON({
		  "functionNames"    : functionNames
		, "renderCacheable"  : renderCacheable
	} ) );
</cfscript>
