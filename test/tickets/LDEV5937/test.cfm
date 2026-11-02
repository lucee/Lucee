<cfscript>
	param name="url.fn";
	switch ( url.fn ) {
		case "onlyInA":
			echo( onlyInA() );
			break;
		case "onlyInB":
			echo( onlyInB() );
			break;
		case "inBoth":
			echo( inBoth() );
			break;
		case "caseDiffers":
			echo( MIXEDcasename() );
			break;
		case "bif":
			echo( ucase( "abc" ) );
			break;
		case "notATemplate":
			try {
				echo( notATemplate() );
			}
			catch ( any e ) {
				echo( "ERROR:" & e.type & ":" & e.message );
			}
			break;
		case "missing":
			try {
				echo( thisFunctionDoesNotExist() );
			}
			catch ( any e ) {
				echo( "ERROR:" & e.type & ":" & e.message );
			}
			break;
	}
</cfscript>
