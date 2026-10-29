<cfscript>
function f() {
	final var x = 1;
	var y = 2;
	y = 3;
	final s = { a: 1 };
	s.b = 2;
	return x & y & structCount( s );
}
function g() {
	final x = 5;
	var cb = function() { var x = 10; x = 11; return x; };
	return x & cb();
}
function h() {
	x = 1; // not final in this function
	x = 2;
	return x;
}
writeOutput( f() & "," & g() & "," & h() );
final x = 100; // template level, other scope than the functions
writeOutput( "," & x );
</cfscript>
