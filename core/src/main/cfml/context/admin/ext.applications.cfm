<cfinclude template="ext.functions.cfm">

<cfparam name="inc" default="">
<cfparam name="url.action2" default="list">
<cfparam name="form.mainAction" default="none">
<cfparam name="url.mainAction" default="">
<cfparam name="form.subAction" default="none">
<cfparam name="session.extFilter.installed" default="">
<cfparam name="session.extFilter.available" default="">

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
</cfif>

<cfparam name="error" default="#struct(message:"",detail:"")#">


<!--- Action --->
<cftry>
<cfscript>
	if(form.mainAction == "none"){
		loop array=form.keyArray() item="k" {
			if(left(k,11)=="mainAction_") {
				form['mainAction']=form[k];
				type=mid(k,11);
				form['version']=form['version'];
			}
		}
	}
</cfscript>
	<cfswitch expression="#form.mainAction#">
		<cfcase value="#stText.Buttons.install#">
        	<cfadmin
			    action="updateRHExtension"
			    type="#request.adminType#"
			    password="#session["password"&request.adminType]#"
				source="#downloadFull(form.provider,form.id,form.version)#">
			<cfset application.reloadPlugins = true>
		</cfcase>
		<cfcase value="#stText.Buttons.upDown#">
			<cfadmin
			    action="updateRHExtension"
			    type="#request.adminType#"
			    password="#session["password"&request.adminType]#"
				source="#downloadFull(form.provider,form.id,form.version)#">
			<cfset application.reloadPlugins = true>
		</cfcase>
        <cfcase value="#stText.Buttons.uninstall#">
        	<cfadmin
			    action="removeRHExtension"
			    type="#request.adminType#"
			    password="#session["password"&request.adminType]#"
				id="#form.id#">
			<cfset application.reloadPlugins = true>
		</cfcase>
		<cfdefaultcase>
			<cfswitch expression="#url.mainAction#">
				<!--- Filter --->
				<cfcase value="#stText.Buttons.filter#">
					<cfif StructKeyExists(url,"filter")
						and htmleditformat(url.filter) eq url.filter>
						<cfset session.extFilter.installed=trim(url.filter)>
					<cfelseif StructKeyExists(url,"filter2")
						and htmleditformat(url.filter2) eq url.filter2>
						<cfset session.extFilter.available=trim(url.filter2)>
					</cfif>
				</cfcase>
				<cfcase value="#stText.Buttons.clearFilter#">
					<cfif StructKeyExists(url,"filter")>
						<cfset session.extFilter.installed="">
					</cfif>
					<cfif StructKeyExists(url,"filter2")>
						<cfset session.extFilter.available="">
					</cfif>
				</cfcase>
			</cfswitch>
		</cfdefaultcase>
	</cfswitch>

	<cfscript>
		if (structKeyExists(application, "reloadPlugins")){
			inspectTemplates(); // flag page pool to be re-inspected for changes
			lock name="lucee_admin_plugins_last_updated"{
				application.plugin = {}; // clear plugin cache
				server.lucee_admin_plugins_last_updated = now(); // used to trigger plugin refresh accross different contexts
			}
		}
	</cfscript>



<cfsavecontent variable="inc"><cfinclude template="#url.action#.#url.action2#.cfm"/></cfsavecontent>
	<cfcatch>
		<cfset error.message=cfcatch.message>
		<cfset error.detail=cfcatch.Detail>
		<cfset error.exception = cfcatch>
		<cfset error.cfcatch=cfcatch>
	</cfcatch>
</cftry>

<!---
Error Output --->
<cfset printError(error)>

<!---
Redirect to entry --->
<cfif cgi.request_method EQ "POST" and error.message EQ "">
	<cflocation url="#request.self#?action=#url.action#&reinit=true" addtoken="no">
</cfif>

<cfoutput>#inc#</cfoutput>


