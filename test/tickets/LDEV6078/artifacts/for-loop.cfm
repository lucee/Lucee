<cfscript>
// LDEV-6078: For loop line numbers

function processItems( required array items ) {
	var total = 0;
	for ( var i = 1; i <= items.len(); i++ ) {
		total += items[ i ];
	}
	return total;
}

systemOutput( processItems( [ 1, 2, 3 ] ), true );
</cfscript>
