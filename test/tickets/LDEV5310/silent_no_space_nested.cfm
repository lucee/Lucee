<cfscript>
	// nested, and inside other statements, function bodies and try/catch
	function inner() {
		silent{
			echo( "hidden function" );
			return "ok";
		}
	}
	if ( true ) silent{
		echo( "hidden if" );
	}
	if ( true ) {
		silent{
			echo( "hidden block" );
			silent{
				echo( "hidden nested" );
			}
		}
	}
	try {
		silent{
			echo( "hidden try" );
		}
		throw "expected";
	}
	catch ( any e ) {
		echo( e.message );
	}
	echo( "-" & inner() & "-" );
	echo( "success" );
</cfscript>