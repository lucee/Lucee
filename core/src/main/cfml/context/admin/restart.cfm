<!--- create no output here!!! --->
<cfsetting showdebugoutput="false">
<cftry>
	<cfadmin 
		action="restart"
		type="#url.adminType#"
		password="#session["password"&url.adminType]#">
		<!---remoteClients="#request.getRemoteClients()#"--->
	<cfcatch>
		<cfdump var="#cfcatch#">
	</cfcatch>
</cftry>
