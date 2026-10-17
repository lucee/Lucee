<cfscript>
	result = {};
	loop list="error,catch,cfcatch" item="k" {
		if ( structKeyExists( variables, k ) ) result[ k ] = variables[ k ];
	}
	echo( serializeJSON( result ) );
</cfscript>
