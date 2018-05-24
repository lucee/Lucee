<cfscript>
stText.Settings.noDriver="There is no driver available for this type, please install a matching Extension for this type.";

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

mysqls=["org.gjt.mm.mysql.Driver","com.mysql.jdbc.Driver","com.mysql.cj.jdbc.Driver"];
mssqls=["com.microsoft.jdbc.sqlserver.SQLServerDriver","com.microsoft.sqlserver.jdbc.SQLServerDriver"];



function getDbDriverTypeName(required className,required dsn) {
	// find matching driver
	loop collection=variables.drivers item="local.key" {
		if(variables.drivers[key].equals(arguments.className,arguments.dsn)) {
			return variables.drivers[key].getName();		
		}
	}

	// mysql
	if(arrayFind(mysqls,className)) {
		loop collection=variables.drivers item="local.key" {
			loop array=mysqls item="local.cn" {
				if(variables.drivers[key].equals(cn,arguments.dsn)) {
					return variables.drivers[key].getName();		
				}
			}
		}
	}

	// mssql
	if(arrayFind(mssqls,className)) {
		loop collection=variables.drivers item="local.key" {
			loop array=mssqls item="local.cn" {
				if(variables.drivers[key].equals(cn,arguments.dsn)) {
					return variables.drivers[key].getName();		
				}
			}
		}
	}

    return variables.drivers['other'].getName();
}


</cfscript>


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