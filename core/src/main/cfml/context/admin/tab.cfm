<cfif thistag.executionMode EQ "start">
	<cfset ctab=GetBaseTagData("CF_TABBEDPANE").attributes.ctab>
	<cfset name=GetBaseTagData("CF_TABBEDPANE").attributes.name>
	
	<cfset thistag.executebody=ListFindNoCase(attributes.name,ctab)>
</cfif>
