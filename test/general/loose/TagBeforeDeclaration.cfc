<!--- Pre-declaration code in tag-syntax — silently discarded by the compiler. Pinned by ../LooseComponent.cfc. --->
<cfset application.looseBeforeCounter = ( application.looseBeforeCounter ?: 0 ) + 1>
<cfset application.looseBeforeUuid    = createUUID()>

<cfcomponent>

	<cffunction name="getBeforeCounter"><cfreturn application.looseBeforeCounter></cffunction>
	<cffunction name="getBeforeUuid"><cfreturn application.looseBeforeUuid></cffunction>

</cfcomponent>
