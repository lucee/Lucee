<cfscript>
	greet = name => { return "Hello " & name; };
	nested = a => b => a + b;
	addOne = nested( 1 );
	writeOutput( greet( "Lucee" ) & "|" & addOne( 2 ) );
</cfscript>
