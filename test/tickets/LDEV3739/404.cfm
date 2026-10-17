<cfscript>
	result = {};
	if ( structKeyExists( variables, "error" ) ) result.error = variables.error;
	echo( serializeJSON( result ) );
</cfscript>
