<cfparam name="FORM.Scene" default="1">
<cfset sessionFlag='adminRunningLock'/>
<cfset i = 1>
<cfset j = 2>
<cfset myVar = structNew()>
<cflock name="#sessionFlag#" type="exclusive" timeout="1" throwOnTimeout="true">
	<cfif FORM.Scene EQ 1>
		<cfloop from="1" to="99999999" index="idx">
			<cfset j = idx>
		</cfloop>
	</cfif>
	<cfset sleep(5000)/>
	<cfif structKeyExists(session,sessionFlag)>
		<cfabort/>
	</cfif>
	<cfset session[sessionFlag]=now()/>
</cflock>