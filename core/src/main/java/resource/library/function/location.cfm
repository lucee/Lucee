<cffunction name="location" output="no"  returntype="void" hint="Stops execution of the current request and rdirect to a other location.">
	<cfargument name="url" type="string" required="yes" hint="URL where the call should redirect">
	<cfargument name="addToken" type="boolean" required="no" default="#true#" hint="appends client variable information to URL (true|false)">
	<cfargument name="statusCode" type="numeric" required="no" default="#302#" hint="The HTTP status code (301,302(default), 303, 304, 305, 307)">
	<cflocation attributeCollection="#arguments#">
</cffunction>