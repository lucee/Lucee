<cfparam name="url.action2" default="none">
<cfswitch expression="#url.action2#">
	<cfcase value="update">
		<cfadmin 
			action="runUpdate"
			type="#request.adminType#"
			password="#session["password"&request.adminType]#">
			<cfset StructClear(session)>
	</cfcase>
</cfswitch>

<cffunction name="getAvailableVersion" output="false">
	<cfif structKeyExists(session,"availableVersion")>
		<cfreturn session.availableVersion>
	</cfif>
	<cfset var http="">
	<cftry>
	<cfhttp 
		url="#update.location#/lucee/remote/version/Info.cfc?method=getpatchversionfor&version=#server.lucee.version#" 
		method="get" resolveurl="no" result="http">
	<cfwddx action="wddx2cfml" input="#http.fileContent#" output="wddx">
	<cfset session.availableVersion=wddx>
	<cfreturn session.availableVersion>
		<cfcatch>
			<cfreturn "">
		</cfcatch>
	</cftry>
</cffunction>

<cffunction name="getAvailableVersionDoc" output="false">
	
	<cfset var http="">
	<cftry>
	<cfhttp 
		url="#update.location#/lucee/remote/version/Info.cfc?method=getPatchVersionDocFor&version=1.0.0.015" 
		method="get" resolveurl="no" result="http"><!--- #server.lucee.version# --->
	<cfwddx action="wddx2cfml" input="#http.fileContent#" output="wddx">
	<cfreturn wddx>
		<cfcatch>
			<cfreturn "">
		</cfcatch>
	</cftry>
</cffunction>
<cfoutput>

<cfadmin 
		action="getUpdate"
		type="#request.adminType#"
		password="#session["password"&request.adminType]#"
		returnvariable="update">

<h2>Update</h2>
<a href="#go(url.action,"update")#">Update to #getAvailableVersion()#</a><br>
Das Update wird in einem eigenen Prozess ablaufen. 
Wenn das System gepatcht wird, werden Sie Ihre Session verliehren und m�ssen Sie sich frisch einloggen.

<br><br>
<h2>Update Info</h2>
 - Installed Version #server.lucee.version#<br>
 - Available Version #getAvailableVersion()#<br>
<form>
<textarea name="doc" rows="30" cols="90">#getAvailableVersionDoc()#</textarea>
</form>
<pre></pre>
</cfoutput>