<cfscript>
	o = new childNoInitmethod();
	writeOutput( ( structKeyExists( o, "parent" ) ? "YES" : "NO" ) & "," & ( structKeyExists( o, "childInit" ) ? "YES" : "NO" ) );
</cfscript>
