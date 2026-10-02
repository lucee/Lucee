<cfscript>
	child                = new child.NamedChild();
	meta                 = getMetadata( child );
	functionNames        = [];
	extendsFunctionNames = [];

	for ( func in ( meta.functions ?: [] ) ) {
		arrayAppend( functionNames, lCase( func.name ) );
	}
	for ( func in ( meta.extends.functions ?: [] ) ) {
		arrayAppend( extendsFunctionNames, lCase( func.name ) );
	}

	echo( serializeJSON({
		  "functionNames"        : functionNames
		, "extendsFunctionNames" : extendsFunctionNames
	} ) );
</cfscript>
