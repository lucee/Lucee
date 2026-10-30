<cfscript>
function f() {
	final var x = 1;
	x = 2;
	return x;
}
writeOutput( f() );
</cfscript>
