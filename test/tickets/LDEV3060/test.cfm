<cfparam name="form.scene" default="1">

<cfif (form.scene eq 1) >
	<cftry>
		<cfset res = getComponentMetadata("InvalidComponent")>
	<cfcatch name="e">
		<cfoutput>#e.message#</cfoutput>
	</cfcatch>
	</cftry>
</cfif>
<cfif (form.scene eq 2) >
	<cftry>
		<cfset res = getComponentMetadata("InvalidComponent1")>
	<cfcatch name="e">
		<cfoutput>#e.message#</cfoutput>
	</cfcatch>
	</cftry>
</cfif>
