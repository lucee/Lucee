<cfscript>
// Edge case 4: exception thrown inside cfscript
try {
	throw( message="Test error", type="TestException" );
} catch( any e ) {
	echo( e.message );
}
</cfscript>
