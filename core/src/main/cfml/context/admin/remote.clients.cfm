<cfset error.message="">
<cfset error.detail="">

<!--- 
Defaults --->
<cfparam name="url.action2" default="list">
<cfparam name="form.mainAction" default="none">
<cfparam name="form.subAction" default="none">
<cfadmin 
	action="securityManager"
	type="#request.adminType#"
	password="#session["password"&request.adminType]#"
	returnVariable="access"
	secType="datasource">
	
<cfif access EQ "yes">
	<cfset access=-1>	
<cfelseif access EQ "none" or access EQ "no">
	<cfset access=0>
</cfif>
	
	
<cfswitch expression="#url.action2#">
	<cfcase value="list"><cfinclude template="remote.clients.list.cfm"/></cfcase>
	<cfcase value="create"><cfinclude template="remote.clients.create.cfm"/></cfcase>

</cfswitch>