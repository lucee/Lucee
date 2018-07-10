<cfcomponent>
	<cfset this.name = hash( getCurrentTemplatePath() )>
	<cfset this.sessionManagement = true>
	<cfset this.clientManagement = true>
</cfcomponent>