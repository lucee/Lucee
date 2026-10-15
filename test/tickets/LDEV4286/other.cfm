<cfscript>
	function test4286b() {
		var x = 1;
		( function() {} );
	}
	function test4286c() {
		() => 1;
	}
	test4286b();
	test4286c();
	writeOutput( "ok" );
</cfscript>
