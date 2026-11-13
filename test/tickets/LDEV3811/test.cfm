<cfscript>
	if ( structKeyExists( url, "requestDebug" ) ) {
		application action="update" debuggingTemplate=true;
	}

	calc = new Calc();
	positional = calc.add( 1, 2 );
	named = calc.add( a=3, b=4 );

	admin action="getDebugData" returnVariable="debugData";

	srcs = [];
	if ( !isNull( debugData ) ) {
		loop query=debugData.pages {
			arrayAppend( srcs, debugData.pages.src );
		}
	}
	echo( serializeJSON( {
		"positional": positional,
		"named": named,
		"hasDebugData": !isNull( debugData ),
		"srcs": srcs
	} ) );
</cfscript>
