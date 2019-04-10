<cfsilent>
	<cfsetting showdebugoutput="false">
<cfinclude template="chartProcess.cfm">
<cfset struct = sysMetric() />
<cfcontent type="application/json">
</cfsilent><cfoutput>#SerializeJSON(struct)#</cfoutput><cfabort />