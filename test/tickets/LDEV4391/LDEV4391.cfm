<cfscript>
	param name="FORM.scene" default="";
	try {
		if (form.scene == 1) {
			function test( ...args ) {
				return args;
			}
			res = test( 5,2,3 );
			writeOutput( arrayToList(res) );
		}
	}
	catch(any e) {
		writeoutput(e.message);
	}
</cfscript>