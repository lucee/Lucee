<cfscript>
	// identifiers starting with silent and a variable named silent are not the silent tag
	silent = { a: 1 };
	silent.b = 2;
	silent[ "c" ] = 3;
	silentX = 4;
	silent2 = { d: 5 };
	function silentFunc() {
		return 6;
	}
	silent{
		echo( "hidden" );
	}
	echo( silent.a & silent.b & silent.c & silentX & silent2.d & silentFunc() );
</cfscript>