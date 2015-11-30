<cfset error.message="">
<cfset error.detail="">

<cfset driverNames=structnew("linked")>
<cfset driverNames=ComponentListPackageAsStruct("lucee-server.admin.dbdriver",driverNames)>
<cfset driverNames=ComponentListPackageAsStruct("lucee.admin.dbdriver",driverNames)>
<cfset driverNames=ComponentListPackageAsStruct("dbdriver",driverNames)>

<cfset variables.drivers=struct()>
<cfset variables.selectors	= struct()>
<cfloop collection="#driverNames#" index="n" item="fn">
	
	<cfif n NEQ "Driver" and n NEQ "IDriver">
		<cfset obj = createObject("component",fn)>
		<cfif isInstanceOf( obj, "types.IDriverSelector" )>
			<cfset variables.selectors[n] = obj>
		<cfelseif isInstanceOf( obj, "types.IDatasource" )>
			<cfset variables.drivers[n] = obj>
		</cfif>
	</cfif>
</cfloop>

<cffunction name="getDbDriverTypeName">
	<cfargument name="className" required="true">
	<cfargument name="dsn" required="true">
	<cfset var key="">
    
	<cfloop collection="#variables.drivers#" item="key">
		<cfif variables.drivers[key].equals(arguments.className,arguments.dsn)>
			<cfreturn variables.drivers[key].getName()>
		</cfif>
	</cfloop>
    
    <cfreturn variables.drivers['other'].getName()>
</cffunction>

<cffunction name="getDbDriverType">
	<cfargument name="className" required="true">
	<cfargument name="dsn" required="true">
	<cfset var key="">
	<cfloop collection="#variables.drivers#" item="key">
		<cfif variables.drivers[key].equals(arguments.className,arguments.dsn)>
			<cfreturn key>
		</cfif>
	</cfloop>
	<cfreturn "other">
</cffunction>

<!--- 
Defaults --->
<cfparam name="url.action2" default="list">
<cfparam name="form.mainAction" default="none">
<cfparam name="form.subAction" default="none">
<cfadmin 
	action="securityManager"
	type="#request.adminType#"
	password="#session["password"&request.adminType]#"
	returnVariable="access"
	secType="datasource">
	
<cfif access EQ "yes">
	<cfset access=-1>	
<cfelseif access EQ "none" or access EQ "no">
	<cfset access=0>
</cfif>
	
	
<cfswitch expression="#url.action2#">
	<cfcase value="list"><cfinclude template="services.datasource.list.cfm"/></cfcase>
	<cfcase value="create"><cfinclude template="services.datasource.create.cfm"/></cfcase>

</cfswitch>