<cfparam name="application.plugin" default="#struct()#">
<cfparam name="application.pluginLanguage.de" default="#struct()#">
<cfparam name="application.pluginLanguage.en" default="#struct()#">
<cfparam name="url.pluginAction" default="overview">
<cfif not structKeyExists(url,"plugin")>
	<cflocation url="#request.self#" addtoken="no">
</cfif>

<!--- avoid concurrency problems when resetting plugins --->
<cflock name="lucee_admin_plugins_last_updated">
	<cfscript>
		if (not StructKeyExists(application.plugin, request.adminType))
			application.plugin[request.adminType] = {};
	</cfscript>

	<!--- load plugin --->
	<cfif not structKeyExists(application.plugin[request.adminType], url.plugin)>
		<cfset application.plugin[request.adminType][url.plugin].application=struct()>
	</cfif>
	<cfif not structKeyExists(application.plugin[request.adminType][url.plugin],'component') or session.alwaysNew>
		<cftry>
			<cfset application.plugin[request.adminType][url.plugin].component=createObject('component','lucee_plugin_directory.'&url.plugin&'.Action')>
			<cfset application.plugin[request.adminType][url.plugin].mapping = "/lucee_plugin_directory">
			<cfcatch>
				<cfif request.adminType eq "web">
					<!--- web contexts inherit the server context settings and plugins --->
					<cfset application.plugin[request.adminType][url.plugin].component=createObject('component','lucee_server_plugin_directory.'&url.plugin&'.Action')>
					<cfset application.plugin[request.adminType][url.plugin].mapping = "/lucee_server_plugin_directory">
				</cfif>
			</cfcatch>
		</cftry>
		<cfset application.plugin[request.adminType][url.plugin].component.init(
				application.pluginLanguage[session.lucee_admin_lang][url.plugin],
				application.plugin[request.adminType][url.plugin].application)>		
	</cfif>
	<cfset plugin=application.plugin[request.adminType][url.plugin]>
	<cfset plugin.language=application.pluginLanguage[session.lucee_admin_lang][url.plugin]>
</cflock>

<cfoutput><cfif not request.disableFrame and structKeyExists(plugin.language,'text') and len(trim(plugin.language.text))>#plugin.language.text#<br /><br /></cfif></cfoutput>

<!--- create scopes --->
<cfset req=duplicate(url)>
<cfset _form=duplicate(form)>
<cfif structKeyExists(_form,'fieldnames')>
	<cfset structDelete(_form,'fieldnames')>
</cfif>
<cfloop collection="#_form#" item="key">
	<cfset req[key]=_form[key]>
</cfloop>
<cfset app=plugin.application>
<cfset lang=plugin.language>

<!---cfset plugin.component._action(plugin:plugin,lang:lang,app:app,req:req)--->

<!--- first call the action if exists --->
<cfset hasAction=structKeyExists(plugin.component,url.pluginAction)>

<cfif hasAction>
	<cfset rtnAction= plugin.component._action(url.pluginAction,lang,app,req)>    
	<!--- cfset rtnAction= plugin.component[url.pluginAction](lang,app,req)--->
</cfif>
<cfif not isDefined('rtnAction')>
	<cfset rtnAction=url.pluginAction>
</cfif>

<!--- redirect --->
<cfif findNoCase('redirect:',rtnAction) EQ 1>
	<cflocation url="#plugin.component.action(mid(rtnAction,10,len(rtnAction)))#" addtoken="no">
</cfif>

<!--- then call display --->
<cfset dspFile="#plugin.mapping#/#url.plugin#/#rtnAction#.cfm">	

<cfset hasDisplay=fileExists(dspFile)>
<cfif rtnAction NEQ "_none" and hasDisplay>
	<cftry>
		<cfset rtnAction= plugin.component._display(dspFile,lang,app,req)>
		<cfcatch>
			<cfset sct=duplicate(cfcatch)>
			<cfset sct.cfcatch=cfcatch>
			<cfset printError(sct,!findNoCase("trial",cfcatch.message))>
		</cfcatch>
	</cftry>
</cfif>

<cfif not hasAction and not hasDisplay>
<cfset printError(struct(message:"there is no action [#url.pluginAction#] or diplay handler [#expandPath(dspFile)#] defined for "&url.plugin,detail:''))>
</cfif>
