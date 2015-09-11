<cfinclude template="ext.functions.cfm">

<cfset stText.ext.free="Free">
<cfset stText.ext.price="Price">
<cfset stText.Buttons.installTrial="Install Trial">
<cfset stText.Buttons.installFull="Install Full Version">
<cfset stText.Buttons.updateTrial="Update as Trial">
<cfset stText.Buttons.updateFull="Update as Full Version">

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
	<cfcatch><cfrethrow>
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


