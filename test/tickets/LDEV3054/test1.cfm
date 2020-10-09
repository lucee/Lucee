<cfapplication name="test1" enableNullSupport="false">

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