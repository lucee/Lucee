<cftry>
	<!--- throws [from:81@gmail.com] cannot be converted to an email address --->
	<cfmail from="from:81@gmail.com" to="xxx@yy.com" subject="test subject">
	 dummy email
	</cfmail>
	<cfcatch>
		<cfoutput>#cfcatch.message#</cfoutput>
		<cfabort>
	</cfcatch>
</cftry>
<cfoutput>ok</cfoutput>
<!---
<cfadmin action="getSpoolerTasks" type="web" password="#server.WEBADMINPASSWORD#" startrow="1" maxrow="1000" result="result" returnVariable="tasks">
<cfset findValue = []>
<cfloop query="tasks">
	<cfset taskDetail = tasks.detail>
	<cfset findValue = structFindValue(taskDetail,"test subject","all")>
</cfloop>

<cfif isEmpty( findValue ) EQ false>
	<cfif taskDetail.from EQ "">
		Null
	<cfelse>	
		<cfoutput>#taskDetail.from#</cfoutput>
	</cfif>
</cfif>
--->