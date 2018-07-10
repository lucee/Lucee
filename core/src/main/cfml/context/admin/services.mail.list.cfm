


<cfoutput>
	
	<!--- Mail Settings
	@todo help text --->
	
	<cfif not hasAccess>
		<cfset noAccess(stText.setting.noAccess)>
	</cfif>
	
	<!--- Common connection settings --->
	<cfinclude template="services.mail.settings.cfm">








<!--- Existing mailservers --->
<cfinclude template="services.mail.serverlist.cfm">






<!--- NEW Server --->


	<cfset data={hostName:"",port:"",username:"",life:60,idle:10}>
	<h2>#stText.mail.createnewMailServerConn#</h2>
	<p>#stText.mail.createnewMailServerConnDesc#</p>
	<cfinclude template="services.mail.form.cfm">





</cfoutput>