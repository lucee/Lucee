<cftry>
    <cfmail from="aaa@bb.com" to="xxx@yyy.com" subject="sample" bcc="bcc:81@gmail.com">dummy email</cfmail>
	<cfcatch>
		<cfdump var="#cfcatch.message#">
	</cfcatch>
</cftry>

<cfadmin 
	action="getSpoolerTasks"
	type="web"
	password="#server.WEBADMINPASSWORD#"
	startrow="1"
	maxrow="1000"
	result="result"
	returnVariable="tasks">

<cfloop query="tasks">
	<cfset taskDetail = tasks.detail>
	<cfset findkey = structFindValue(taskDetail,"sample","all")>
</cfloop>
	
<cfif arrayisempty(findkey) EQ false>
	<cfif taskDetail.bcc EQ "">
		Null
	<cfelse>	
		<cfoutput>#taskDetail.bcc#</cfoutput>
	</cfif>
</cfif>