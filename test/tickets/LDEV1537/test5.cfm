<cftry>
	<!--- will throw invalid cc address --->
	<cfmail from="aaa@bb.com" to="xxx@yyy.com" subject="sample" cc="cc:81@gmail.com">dummy email</cfmail>
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
	
<cfif isEmpty ( findkey ) EQ false>
	<cfif taskDetail.cc EQ "">
		Null
	<cfelse>	
		<cfoutput>#taskDetail.cc#</cfoutput>
	</cfif>
</cfif>
--->