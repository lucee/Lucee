<cfscript>
// LDEV-6078: While loop pattern
// Bug: while condition and first statement inside may share same label

function sumToN( required numeric n ) {
	var sum = 0;
	var i = 1;
	while ( i <= n ) {
		sum += i;
		i++;
	}
	return sum;
}

systemOutput( sumToN( 10 ), true );
</cfscript>
