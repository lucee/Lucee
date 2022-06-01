<cfcomponent>
	<cfset this.name = hash( getCurrentTemplatePath() )>
	<cfset this.sessionManagement = true>
	<cfset this.clientManagement = true>
<cfscript>

	public function onRequestStart() {
		setting requesttimeout=10;
	}
</cfscript>
</cfcomponent>