<!--- Tag-syntax pseudo-constructor body — same path as script syntax. Statements interspersed between cffunction declarations. --->
<cfcomponent>

	<cffunction name="getBetweenUuid"><cfreturn variables.betweenUuid></cffunction>

	<cfset variables.betweenUuid    = createUUID()>
	<cfset variables.betweenCounter = ( application.looseBetweenCounter ?: 0 ) + 1>
	<cfset application.looseBetweenCounter = variables.betweenCounter>

	<cffunction name="getBetweenCounter"><cfreturn variables.betweenCounter></cffunction>

</cfcomponent>
