<cfscript>
	fn0 = () =>;
	fn1 = ( a ) =>;
	fns = [ () =>, () => 1 ];
	writeOutput( isNull( fn0() ) & "," & isNull( fn1( 1 ) ) & "," & arrayLen( fns ) );
</cfscript>
