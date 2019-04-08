<cfset error.message="">
<cfset error.detail="">
<cfparam name="url.action2" default="list">
<cfparam name="form.mainAction" default="none">
<cfparam name="form.subAction" default="none">


<cfadmin 
	action="getDebugEntry"
	type="#request.adminType#"
	password="#session["password"&request.adminType]#"
	returnVariable="debug">
    

<cfadmin 
	action="getDebug"
	type="#request.adminType#"
	password="#session["password"&request.adminType]#"
	returnVariable="_debug">
    
<cfadmin 
	action="securityManager"
	type="#request.adminType#"
	password="#session["password"&request.adminType]#"
	returnVariable="access"
	secType="debugging">
<cfset hasAccess=access>

    
<!--- load available drivers --->
<cfset driverNames=structnew("linked")>
<cfset driverNames=ComponentListPackageAsStruct("lucee-server.admin.debug",driverNames)>
<cfset driverNames=ComponentListPackageAsStruct("lucee.admin.debug",driverNames)>
<cfset driverNames=ComponentListPackageAsStruct("debug",driverNames)>


<cfset drivers={}>
    <cfloop collection="#driverNames#" index="n" item="fn">
    	<cfif n EQ "Debug" or n EQ "Field" or n EQ "Group">
        	<cfcontinue>
        </cfif>
    	<cfset tmp=createObject('component',fn)>
   		<cfset drivers[trim(tmp.getId())]=tmp>
    </cfloop>	
<!--- 
<span class="CheckError">
The Gateway Implementation is currently in Beta State. Its functionality can change before it's final release.
If you have any problems while using the Gateway Implementation, please post the bugs and errors in our <a href="https://jira.jboss.org/jira/browse/Lucee" target="_blank" class="CheckError">bugtracking system</a>. 
</span><br /><br />
--->


<cfswitch expression="#url.action2#">
	<cfcase value="list"><cfinclude template="debugging.templates.list.cfm"/></cfcase>
	<cfcase value="create"><cfinclude template="debugging.templates.create.cfm"/></cfcase>

</cfswitch>