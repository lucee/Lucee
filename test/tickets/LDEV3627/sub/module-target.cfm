<cfif thisTag.executionMode is "start">
	<cfparam name="attributes.value" default="0">
	<cfoutput>module:#attributes.value#</cfoutput>
</cfif>
