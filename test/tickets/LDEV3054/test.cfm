<cfapplication name="test" enableNullSupport="true">

<cfscript>
	try {
		function test(){
		}
		t=test();
		writeOutput(t);
	}
	catch (any e) {
		writeOutput(e.message);
	}
</cfscript>