
<cfset data=queryRowData(ms,url.row)>
<cfoutput>
	<h2>#stText.mail.editMailServerConn#</h2>
	<p>#stText.mail.editMailServerConnDesc#</p>
	<cfinclude template="services.mail.form.cfm">
</cfoutput>