<cfset error.message="">
<cfset error.detail="">
<cfparam name="url.action2" default="list">
<cfparam name="form.mainAction" default="none">
<cfparam name="form.subAction" default="none">


<cfset stText.debug.label="Label">
<cfset stText.debug.type="Type">
<cfset stText.debug.labelMissing="you need to define the label for the debug template">
<cfset stText.debug.noDriver="there is no debug template defined">
<cfset stText.debug.noAccess="you have no access to manipulate the debug settings">
<cfset stText.debug.ipRange="IP Range">
<cfset stText.debug.ipRangeDesc="A comma separeted list of strings of ip defintions. The following patterns are allowed:
- * including all ips
- a single ip, like ""127.0.0.1"" or ""0:0:0:0:0:0:0:1%0""
- an ip with wildcards like ""127.0.0.*"", in this case all ips between ""127.0.0.0"" and ""127.0.0.255"" are valid
- an ip range like ""127.0.0.1-127.0.0.10"", in this case all ips between ""127.0.0.1"" and ""127.0.0.10"" are valid
You can define IPv4 or IPv6 IPs, a IPv4 can not be converted to a IPv6 and visa versa.">
<cfset stText.debug.ipRangeMIssing="Missing IP Range defintion">
<cfset stText.debug.addMyIp="Add my IP">

<cfset stText.debug.list.serverTitle="Readonly Debug Templates">
<cfset stText.debug.list.webTitle="Debug Templates">
<cfset stText.debug.list.serverTitleDesc="Readonly debug templates are generated within the ""server administrator"" for all web instances and can not be modified by the ""web administrator"".">
<cfset stText.debug.list.webTitleDesc="list of existing debug templates defined.">
<cfset stText.debug.list.createDesc="define a debug template, to show the debug information at the end of a request, defining a template is not necessary to log the debug information.">
<cfset stText.debug.createTitle="Create a Template for a specific IP Range">

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
	<cfcase value="ipedit"><cfinclude template="debugging.templates.ipedit.cfm"/></cfcase>
</cfswitch>