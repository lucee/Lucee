<cfscript>
// LDEV-6078: Switch/case statements
// Bug: case labels and statements may share same bytecode label

function getDay( required numeric dayNum ) {
	var result = "";
	switch ( arguments.dayNum ) {
		case 1:
			result = "Monday";
			break;
		case 2:
			result = "Tuesday";
			break;
		case 3:
			result = "Wednesday";
			break;
		default:
			result = "Unknown";
	}
	return result;
}

systemOutput( getDay( 2 ), true );
</cfscript>
