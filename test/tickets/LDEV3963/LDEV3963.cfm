<cfscript>
	try {
		function foo() secured:api {}
		writeOutput(structKeyExists(getMetadata( foo ),"secured:api"))
	}
	catch(any e) {
		writeOutput(e.message) 
	}
</cfscript>