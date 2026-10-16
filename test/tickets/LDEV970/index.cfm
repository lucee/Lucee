<cfscript>
	param url.method = "bracket";
	param url.pc = "";
</cfscript>

<cfif url.method eq "dot">
	<cfscript>cookie.testCookie = "value";</cfscript>
<cfelseif url.method eq "bracket">
	<cfscript>cookie[ 'testCookie' ] = "value";</cfscript>
<cfelseif url.method eq "struct">
	<cfscript>
		if ( len( url.pc ) )
			cookie[ 'testCookie' ] = { value: "value", preservecase: url.pc };
		else
			cookie[ 'testCookie' ] = { value: "value" };
	</cfscript>
<cfelseif url.method eq "tag">
	<cfif len( url.pc )>
		<cfcookie name="testCookie" value="value" preservecase="#url.pc#">
	<cfelse>
		<cfcookie name="testCookie" value="value">
	</cfif>
</cfif>
