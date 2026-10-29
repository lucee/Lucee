<cfscript>
function f( a = 0 ) { return a; }
s = { a: 1 };
x = f( ...s );
</cfscript>
