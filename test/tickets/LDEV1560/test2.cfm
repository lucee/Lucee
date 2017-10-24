<cfscript>
	testObj = new test( 
		alice = 12,
		bob = false
	);
</cfscript>
<cfoutput>#serializeJSON(testObj)#</cfoutput>