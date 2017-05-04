<cfset data=queryRowData(ms,url.row)>
<cfoutput>
	<!--- Existing mailservers --->
	<cfinclude template="services.mail.serverlist.cfm">

	<!--- Edit Server --->
	<h2>#stText.mail.editMailServerConn#</h2>
	<p>#stText.mail.editMailServerConnDesc#</p>
	<cfinclude template="services.mail.form.cfm">
</cfoutput>