<cfscript>
	cfc =  new LDEV3672_final();
	try {
		savecontent variable="ignore"{
			dump( cfc );
		}
		echo( "success" );
	} catch( e ){
		echo( e.stacktrace );
	}
</cfscript>