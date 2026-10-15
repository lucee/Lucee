<cfscript>
	function test3020c( item ) {
		return isNull( arguments.item?.response?.choices );
	}
	settings = {};
	x = {};
	writeOutput( test3020c() & "," & isNull( settings?.datasource?.type ) & "," & isNull( x?.v ) );
</cfscript>
