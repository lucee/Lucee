<cfinterface extends="IDriver">


	<cffunction name="equals" returntype="boolean" output="false"
		hint="returns true if a passed class uses the same driver as this one">

		<cfargument name="className" required="true">
		<cfargument name="dsn" required="true">
		
	</cffunction>
	
	<cffunction name="getClass" returntype="string" output="no" 
		hint="returns the Java driver Class">

	</cffunction>
	
	<!--- TODO: rename getDSN to getConnectionString !--->
	<cffunction name="getDSN" returntype="string" output="no" 
		hint="returns the Connection String of the datasource">
		
	</cffunction>


</cfinterface>