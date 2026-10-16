<cfcomponent>

	<cffunction name="getAfterCounter"><cfreturn application.looseAfterCounter ?: 0></cffunction>
	<cffunction name="getAfterUuid"><cfreturn application.looseAfterUuid    ?: ""></cffunction>

</cfcomponent>

<!--- Post-declaration code in tag-syntax — silently discarded by the compiler. Pinned by ../LooseComponent.cfc. --->
<cfset application.looseAfterCounter = ( application.looseAfterCounter ?: 0 ) + 1>
<cfset application.looseAfterUuid    = createUUID()>
