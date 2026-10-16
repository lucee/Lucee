<cfscript>
// LDEV-6078: Throw statement line numbers

function validate( required any value ) {
	if ( isNull( value ) ) {
		throw( type="ValidationError", message="Value cannot be null" );
	}
	if ( !isSimpleValue( value ) ) {
		throw( type="ValidationError", message="Value must be simple" );
	}
	return true;
}

try {
	systemOutput( validate( "test" ), true );
} catch ( any e ) {
	systemOutput( e.message, true );
}
</cfscript>
