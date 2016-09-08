<cfinclude template="ext.functions.cfm">

<cfscript>
stText.ext.installedInServer="Extension installed in the Server Administrator";
stText.ext.minLuceeVersion="Lucee Version";
stText.ext.minLuceeVersionDesc="Minimal Lucee Version needed for this Extension.";
stText.ext.toSmallVersion="You need at least the Lucee {version} to install this Extension";

stText.ext.availableVersions="Available Versions";
stText.ext.updateTo="update to";
stText.ext.updateTrialTo="update as trial to";
stText.ext.updateFullTo="update as full version to";
stText.ext.downgradeTo="downgrade to";

stText.ext.install="Install";
stText.ext.upDown="Update / Downgrade / Uninstall";
stText.ext.uninstall="Uninstall";
stText.ext.installDesc="Choose the version you would like to install.";
stText.ext.upDownDesc="Update or downgrade your already installed version.";
stText.ext.uninstallDesc="Uninstall this version";


stText.Buttons.go="go";

stText.Buttons.upDown="update / downgrade";

</cfscript>



<cfparam name="inc" default="">
<cfparam name="url.action2" default="list">
<cfparam name="form.mainAction" default="none">
<cfparam name="form.subAction" default="none">
<cfif not isDefined('session.extFilter2')>
	<cfset session.extFilter.filter="">
	<cfset session.extFilter.filter2="">
	<cfset session.extFilter.category="">
	<cfset session.extFilter.name="">
	<cfset session.extFilter.provider="">
	<cfset session.extFilter2.category="">
	<cfset session.extFilter2.name="">
	<cfset session.extFilter2.provider="">
</cfif>

<cfadmin 
	action="getRHExtensionProviders"
	type="#request.adminType#"
	password="#session["password"&request.adminType]#"
	returnVariable="providers">
<cfset providerURLs=queryColumnData(providers,"url")>
<cfset request.providers=providers>


<cfadmin 
    action="getRHExtensions"
    type="#request.adminType#"
    password="#session["password"&request.adminType]#"
    returnVariable="extensions">


<cfif request.adminType=="web">
	<cfadmin 
	    action="getRHServerExtensions"
	    type="#request.adminType#"
	    password="#session["password"&request.adminType]#"
	    returnVariable="serverExtensions">
	<!---<cfset extensions=queryNew(serverExtensions.columnlist&",installLocation")>
	<cfset ids={}>
	 server 
	<cfloop query=serverExtensions>
		<cfset row=queryAddrow(extensions)>
		<cfset ids[serverExtensions.id]="">
		<cfset extensions.installLocation[row]="server">
		<cfloop array="#queryColumnArray(serverExtensions)#" item="col">
			<cfset extensions[col][row]=serverExtensions[col]>
		</cfloop>
	</cfloop>--->
	<!--- web
	<cfloop query=webExtensions>
		<cfif !isNull(ids[webExtensions.id])>
			<cfcontinue>
		</cfif>
		<cfset row=queryAddrow(extensions)>
		<cfset extensions.installLocation[row]="web">
		<cfloop array="#queryColumnArray(webExtensions)#" item="col">
			<cfset extensions[col][row]=webExtensions[col]>
		</cfloop>
	</cfloop> --->

</cfif>




<cfparam name="error" default="#struct(message:"",detail:"")#">


<!--- Action --->
<cftry>

	<cfswitch expression="#form.mainAction#">
	<!--- Filter --->
		<cfcase value="#stText.Buttons.filter#">
        	<cfif StructKeyExists(form,"filter")>
				<cfset session.extFilter.filter=trim(form.filter)>
            <cfelseif StructKeyExists(form,"filter2")>
				<cfset session.extFilter.filter2=trim(form.filter2)>
            <cfelseif StructKeyExists(form,"categoryFilter")>
				<cfset session.extFilter.category=trim(form.categoryFilter)>
                <cfset session.extFilter.name=trim(form.nameFilter)>
                <cfset session.extFilter.provider=trim(form.providerFilter)>
            <cfelse>
				<cfset session.extFilter2.category=trim(form.categoryFilter2)>
                <cfset session.extFilter2.name=trim(form.nameFilter2)>
                <cfset session.extFilter2.provider=trim(form.providerFilter2)>
            </cfif>
		</cfcase>
		<!---
        <cfcase value="#stText.Buttons.install#,#stText.Buttons.installFull#">
        	<cfadmin 
			    action="updateRHExtension"
			    type="#request.adminType#"
			    password="#session["password"&request.adminType]#"
			    source="#downloadFull(form.provider,form.id)#">
		</cfcase>
        <cfcase value="#stText.Buttons.update#,#stText.Buttons.updateFull#">
			<cfadmin 
			    action="updateRHExtension"
			    type="#request.adminType#"
			    password="#session["password"&request.adminType]#"
			    source="#downloadFull(form.provider,form.id)#">
		</cfcase>
        <cfcase value="#stText.Buttons.installTrial#">
        	<cfadmin 
			    action="updateRHExtension"
			    type="#request.adminType#"
			    password="#session["password"&request.adminType]#"
			    source="#downloadTrial(form.provider,form.id)#">
		</cfcase>
        <cfcase value="#stText.Buttons.updateTrial#">
        	<cfadmin 
			    action="updateRHExtension"
			    type="#request.adminType#"
			    password="#session["password"&request.adminType]#"
			    source="#downloadTrial(form.provider,form.id)#">
		</cfcase>--->

		<cfcase value="#stText.Buttons.install#">
        	<cfadmin 
			    action="updateRHExtension"
			    type="#request.adminType#"
			    password="#session["password"&request.adminType]#"
			    source="#downloadFull(form.provider,form.id,form.version)#">
		</cfcase>
		<cfcase value="#stText.Buttons.upDown#">
        	<cfadmin 
			    action="updateRHExtension"
			    type="#request.adminType#"
			    password="#session["password"&request.adminType]#"
			    source="#downloadFull(form.provider,form.id,form.version)#">
		</cfcase>
        <cfcase value="#stText.Buttons.uninstall#">
        	<cfadmin 
			    action="removeRHExtension"
			    type="#request.adminType#"
			    password="#session["password"&request.adminType]#"
			    id="#form.id#">
		</cfcase>
	</cfswitch>
<cfsavecontent variable="inc"><cfinclude template="#url.action#.#url.action2#.cfm"/></cfsavecontent>
	<cfcatch>
		<cfset error.message=cfcatch.message>
		<cfset error.detail=cfcatch.Detail>
	</cfcatch>
</cftry>

<!--- 
Error Output --->
<cfset printError(error)>

<!--- 
Redirtect to entry --->
<cfif cgi.request_method EQ "POST" and error.message EQ "">
	<cflocation url="#request.self#?action=#url.action#" addtoken="no">
</cfif>

<cfoutput>#inc#</cfoutput>


