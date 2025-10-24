<cfscript>
	o = new childDifferent();
	writeOutput( ( structKeyExists( o, "parent" ) ? "YES" : "NO" ) & "," & ( structKeyExists( o, "child" ) ? "YES" : "NO" ) );
</cfscript>
