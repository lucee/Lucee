<cfinterface>

	
	<cffunction name="getName" returntype="string"  output="no"
		hint="returns display name of the driver">
		
	</cffunction>
	
	<cffunction name="getDescription" returntype="string"  output="no"
		hint="returns description for the driver">
		
	</cffunction>

	
	<!---
	<cffunction name="equals" returntype="string" output="no" 
		hint="return if String class match this">
		<cfargument name="className" required="true">
		<cfargument name="dsn" required="true">
	</cffunction>
	
	<cffunction name="getType" returntype="numeric" output="no">
		<cfargument name="key" required="true" type="string">
	</cffunction>
	
	<cffunction name="getValue" returntype="string" output="no">
		<cfargument name="key" required="true" type="string">
	</cffunction>
	
	<cffunction name="getClass" returntype="string" output="no" 
		hint="return driver Java Class">
	</cffunction>
	
	<cffunction name="getDSN" returntype="string"  output="no"
		hint="return DSN">
	</cffunction>
	
	<cffunction name="onBeforeUpdate" returntype="void" output="no">
	</cffunction>
	
	<cffunction name="onBeforeError" returntype="void" output="no">
		<cfargument name="cfcatch" required="true" type="struct">
	</cffunction>
	
	<cffunction name="init" returntype="void" output="no">
		<cfargument name="data" required="yes" type="struct">
	</cffunction>
	--->
</cfinterface>