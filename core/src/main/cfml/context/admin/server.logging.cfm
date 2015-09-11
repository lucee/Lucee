<cfset error.message="">
<cfset error.detail="">
<cfparam name="url.action2" default="list">
<cfparam name="form.mainAction" default="none">
<cfparam name="form.subAction" default="none">	
	
<cfadmin 
        action="getLogSettings" 
        type="#request.adminType#"
        password="#session["password"&request.adminType]#"
        
        returnVariable="logs"
        remoteClients="#request.getRemoteClients()#">

<!--- load available appenders --->
<cfset appenders={}>
<cfset names=structnew("linked")>
<cfset names=ComponentListPackageAsStruct("lucee-server.admin.logging.appender",names)>
<cfset names=ComponentListPackageAsStruct("lucee.admin.logging.appender",names)>
<cfset names=ComponentListPackageAsStruct("logging.appender",names)>
<cfloop collection="#names#" index="n" item="fn">
	<cfif n NEQ "Appender" and n NEQ "Field" and n NEQ "Group">
		<cfset tmp = createObject("component",fn)>
		<cfset appenders[tmp.getClass()]=tmp>
	</cfif>
</cfloop>
 
<!--- load available layouts --->
<cfset layouts={}>
<cfset names=structnew("linked")>
<cfset names=ComponentListPackageAsStruct("lucee-server.admin.logging.layout",names)>
<cfset names=ComponentListPackageAsStruct("lucee.admin.logging.layout",names)>
<cfset names=ComponentListPackageAsStruct("logging.layout",names)>
<cfloop collection="#names#" index="n" item="fn">
	<cfif n NEQ "Layout" and n NEQ "Field" and n NEQ "Group">
		<cfset tmp = createObject("component",fn)>
		<cfset layouts[tmp.getClass()]=tmp>
	</cfif>
</cfloop>


<cfset access=true>
<!--- TODO
<cfadmin 
	action="securityManager"
	type="#request.adminType#"
	password="#session["password"&request.adminType]#"
	returnVariable="access"
	secType="logging">
	 --->
	

<cfswitch expression="#url.action2#">
	<cfcase value="list"><cfinclude template="server.logging.list.cfm"/></cfcase>
	<cfcase value="create"><cfinclude template="server.logging.create.cfm"/></cfcase>

</cfswitch>