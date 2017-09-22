<cfscript>

error.message="";
error.detail="";

driverNames=structnew("linked");
driverNames=ComponentListPackageAsStruct("lucee-server.admin.dbdriver",driverNames);
driverNames=ComponentListPackageAsStruct("lucee.admin.dbdriver",driverNames);
driverNames=ComponentListPackageAsStruct("dbdriver",driverNames);

variables.drivers={};
variables.selectors	= {};
loop collection=driverNames index="n" item="fn" {
	if(n!="Driver" && n!="IDriver") {
		obj = createObject("component",fn);
		if(isInstanceOf( obj, "types.IDriverSelector" ))
			variables.selectors[n] = obj;
		else if(isInstanceOf( obj, "types.IDatasource" ))
			variables.drivers[n] = obj;
	}
}

installed={};
loop collection=drivers index="n" item="dr" {
	try {
		createObject("java",dr.getClass());
		installed[dr.getClass()]=true;
	}
	catch(e){
		installed[dr.getClass()]=false;
	}
}

</cfscript>

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