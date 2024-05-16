<!--- create no output here!!! --->
<cfsetting showdebugoutput="false">
<cftry>
	<cfsetting requesttimeout="100000">
	<cfadmin
		action="changeVersionTo"
        version="#url.version#"
		type="#url.adminType#"
		password="#session["password"&url.adminType]#">

	<cfcatch>
		<cfset echo(cfcatch.message)>
	</cfcatch>
</cftry>
<cfabort>