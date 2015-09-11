<cfset datasources=getPageContext().getConfig().getDatasources()>


<table class="darker" cellpadding="2" cellspacing="1">
<tr>
	<td>Name</td>
	<td>DNS</td>
</tr>
<cfoutput><cfloop collection="#datasources#" item="key">
<cfif key NEQ "_queryofquerydb">
<cfset datasource=datasources[key]>
<tr>
	<td class="brigther">#key#</td>
	<td class="brigther">#datasource.getDSN()#</td>
</tr>
</cfif>
</cfloop></cfoutput>
</table>
