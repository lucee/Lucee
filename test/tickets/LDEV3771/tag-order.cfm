<cfscript>
	param name="url.type" default="a";
</cfscript>
<cftry>
	<cfthrow type="#url.type#" message="boom">
	<cfcatch type="a" | "b" name="ex">
		<cfoutput>first:#ex.type#</cfoutput>
	</cfcatch>
	<cfcatch type="c" name="ex">
		<cfoutput>second:#ex.type#</cfoutput>
	</cfcatch>
	<cfcatch type="any" name="ex">
		<cfoutput>any:#ex.type#</cfoutput>
	</cfcatch>
</cftry>
