<cfscript>
	hasAccess=true;
</cfscript>

<cfset csss={
	active:"background-color:##cfc",
	installed:"background-color:##ffc",
	notinstalled:"background-color:##fcc",
	resolved:"background-color:##ff9"

	}>


<cfparam name="url.action2" default="list">
<cfparam name="form.mainAction" default="none">
<cfparam name="form.subAction" default="none">	

<cfswitch expression="#url.action2#">
	<cfcase value="list"><cfinclude template="info.bundle.list.cfm"/></cfcase>
	<cfcase value="create"><cfinclude template="info.bundle.create.cfm"/></cfcase>
</cfswitch>