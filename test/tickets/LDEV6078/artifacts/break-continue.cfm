<cfscript>
// LDEV-6078: Break and continue statements in loops

function findFirst( required array items, required numeric target ) {
	var found = -1;
	for ( var i = 1; i <= items.len(); i++ ) {
		if ( items[ i ] < 0 ) {
			continue;
		}
		if ( items[ i ] == target ) {
			found = i;
			break;
		}
	}
	return found;
}

systemOutput( findFirst( [ -1, 2, 3, 4 ], 3 ), true );
</cfscript>
