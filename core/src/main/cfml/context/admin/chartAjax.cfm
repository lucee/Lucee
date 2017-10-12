<cfinclude template="chartProcess.cfm">
<cfset struct = sysMetric() />
<cfheader name="Content-Type" value="application/json" />
<cfoutput>#SerializeJSON(struct)#</cfoutput><cfabort />