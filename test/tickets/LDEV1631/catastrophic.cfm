<cfsetting requesttimeout="2">
<cfscript>
	// nested quantifiers with a back reference (java memoizes the ones without), the number of steps doubles with every additional "a"
	str = repeatString( "a", val( url.len ) ) & "b";
	start = getTickCount();
	if ( url.fn == "reFind" ) reFind( "(a+)+\1c", str );
	else if ( url.fn == "reFindNoCase" ) reFindNoCase( "(A+)+\1c", str );
	else if ( url.fn == "reMatch" ) reMatch( "(a+)+\1c", str );
	else if ( url.fn == "reMatchNoCase" ) reMatchNoCase( "(A+)+\1c", str );
	else if ( url.fn == "reReplace" ) reReplace( str, "(a+)+\1c", "x", "all" );
	else if ( url.fn == "reReplaceNoCase" ) reReplaceNoCase( str, "(A+)+\1c", "x", "all" );
	else if ( url.fn == "isValid" ) isValid( "regex", str, "^(a+)+\1c$" );
	else throw( message="unknown function [#url.fn#]" );
	writeOutput( "finished after #getTickCount() - start# ms" );
</cfscript>
