<cfscript>
function f() {
	final x = 1;
	x++;
	return x;
}
writeOutput( f() );
</cfscript>
