<cftry>
	<!--- will throw invalid bcc address --->
	<cfmail from="aaa@bb.com" to="xxx@yyy.com" subject="sample" bcc="bcc:81@gmail.com">dummy email</cfmail>
	<cfcatch>
		<cfoutput>#cfcatch.message#</cfoutput>
		<cfabort>
	</cfcatch>
</cftry>
<cfoutput>ok</cfoutput>
<!---

<cfadmin 
	action="getSpoolerTasks"
	type="web"
	password="#server.WEBADMINPASSWORD#"
	startrow="1"
	maxrow="1000"
	result="result"
	returnVariable="tasks">

<cfset findkey = []>
<cfloop query="tasks">
	<cfset taskDetail = tasks.detail>
	<cfset findkey = structFindValue(taskDetail,"sample","all")>
</cfloop>
	
<cfif isEmpty( findkey ) EQ false>
	<cfif taskDetail.bcc EQ "">
		Null
	<cfelse>	
		<cfoutput>#taskDetail.bcc#</cfoutput>
	</cfif>
</cfif>
--->