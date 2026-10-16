<cfscript>
	function test3020b() {
		var x = 0;
		if ( false ) x \= 1;
		var y = 9;
		if ( true ) y \= 2;
		return x & "," & y;
	}
	writeOutput( test3020b() );
</cfscript>
