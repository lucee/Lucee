<cfscript>
	function test3020() {
		var x = 7;
		x \= 2;
		return x;
	}
	s = { a: { b: 10 } };
	s.a.b \= 3;
	writeOutput( test3020() & "," & s.a.b );
</cfscript>
