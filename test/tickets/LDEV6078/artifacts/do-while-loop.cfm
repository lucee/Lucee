<cfscript>
// LDEV-6078: Do-while loop pattern
// Tests line number bug with do { and first statement inside
// Bug: do { line and first statement inside get same label

function processItems( required array items ) {
	var index = 1;
	var result = "";
	do {
		result &= items[ index ];
		index++;
	} while ( index <= items.len() );
	return result;
}

systemOutput( processItems( [ "a", "b", "c" ] ), true );
</cfscript>
