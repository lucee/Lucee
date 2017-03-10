<cfscript>
	o = new child();
	try {
		writeOutput( o.Const1+o.Const3 );
	}
	catch( any e ) {
		writeOutput( e.Message );
	}
</cfscript>