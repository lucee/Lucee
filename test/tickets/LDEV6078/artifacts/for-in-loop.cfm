<cfscript>
// LDEV-6078: For-in loop line numbers

function sumValues( required struct data ) {
	var total = 0;
	for ( var key in data ) {
		total += data[ key ];
	}
	return total;
}

systemOutput( sumValues( { a: 1, b: 2, c: 3 } ), true );
</cfscript>
