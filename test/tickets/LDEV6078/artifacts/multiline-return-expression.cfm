<cfscript>
// LDEV-6078: Multiline return expression
// Bug: return ( and first expression line share same label

function checkAccess( required string action ) {
	return (
		( structKeyExists( this, arguments.action ) && isCustomFunction( this[ arguments.action ] ) )
		||
		( structKeyExists( variables, arguments.action ) && isCustomFunction( variables[ arguments.action ] ) )
	);
}

function getValue( required numeric x ) {
	return (
		x > 100
		? "large"
		: x > 50
			? "medium"
			: "small"
	);
}

systemOutput( checkAccess( "test" ), true );
systemOutput( getValue( 75 ), true );
</cfscript>
