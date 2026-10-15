<cfscript>
	amount = 2;
	writeOutput( precisionEvaluate( 3 * ( 3 ^ 3 ) ) & "," & precisionEvaluate( 2 ^ 3 + 1 ) & "," & numberFormat( precisionEvaluate( amount * ( ( 3.2808333333 ) ^ 3 ) ), "0.00" ) );
</cfscript>
