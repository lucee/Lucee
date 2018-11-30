<cfif FORM.Scene EQ 1>
	<cfset myArray = [3]>
	<cftry>
		<cfloop array=myArray index="i">
			<cfoutput>#i#</cfoutput>
		</cfloop>
		<cfcatch type="any">
			<cfoutput>#cfcatch.Message#</cfoutput>
		</cfcatch>
	</cftry>
<cfelseif FORM.Scene EQ 2>
	<cfset myList = "lucee">
	<cfloop list=myList index="j">
		<cfoutput>#j#</cfoutput>
	</cfloop>
<cfelseif FORM.Scene EQ 3>
	<cfset myStct = {"name":"lucee"}> 
	<cfloop collection=myStct item="k">
		<cfoutput> #k#</cfoutput>
	</cfloop>
</cfif>