<cfscript>
// Edge case 2: cfscript with function at the end (not extracted first)
x = 10;
echo( x );

function laterFunction() {
	return 42;
}
</cfscript>
