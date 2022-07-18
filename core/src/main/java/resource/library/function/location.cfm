<cffunction name="location" output="no"  returntype="void" hint="Stops execution of the current request and redirect to another location.">
	<cfargument name="url" type="string" required="yes" hint="URL where the call should redirect">
	<cfargument name="addToken" type="boolean" required="no" default="#false#" hint="appends client variable information to URL (true|false)">
	<cfargument name="statusCode" type="numeric" required="no" default="#302#" hint="The HTTP status code (301,302(default), 303, 304, 305, 307)">
	<cfargument name="encode" type="boolean" required="no" default="true" hint="Encode the given URL value">
	<cflocation attributeCollection="#arguments#">
</cffunction>