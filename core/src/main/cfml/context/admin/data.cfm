<cftry>
	<cfset datasources=getPageContext().getConfig().getDatasources()>
	<cfcatch>
		<cfadmin 
			action="getDatasources"
			type="#request.adminType#"
			password="#session["password"&request.adminType]#"
			returnVariable="datasources">
	</cfcatch>
</cftry>

<table class="darker" cellpadding="2" cellspacing="1">
<tr>
    <td>Name</td>
    <td>DSN</td>
</tr>
<cfoutput>
    <cfloop query="#datasources#">
        <cfif !(datasources.keyExists('_queryofquerydb'))>
            <tr>
                <td class="brigther">#datasources.currentrow#</td>
                <td class="brigther">#datasources.name#</td>
            </tr>
        </cfif>
    </cfloop>
</cfoutput>
</table>