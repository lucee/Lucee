<cfscript>
	param name="url.type" default="b";
	result = "";
</cfscript>
<cftry>
	<cfthrow type="#url.type#" message="boom">
	<cfcatch type="a" | "b" | "c" name="ex">
		<cfset result = "caught:" & ex.type>
	</cfcatch>
</cftry>
<cfoutput>#result#</cfoutput>
