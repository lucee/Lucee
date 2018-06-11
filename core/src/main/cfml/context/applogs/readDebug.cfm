<cfsetting showdebugoutput="false">
<cfif isNull(url.id)>
	<cfadmin
		action="getLoggedDebugData"
		type="web"
		returnVariable="all">
	<cfset url.id=all[arrayLen(all)].id>
</cfif>

<cfadmin
	action="getLoggedDebugData"
	type="web"
	id="#url.id#"
	returnVariable="log">

<cfadmin
	action="getDebugEntry"
	type="web"
	returnVariable="entries">

<cfset driverNames=structnew("linked")>
<cfset driverNames=ComponentListPackageAsStruct("lucee-server.admin.debug",driverNames)>
<cfset driverNames=ComponentListPackageAsStruct("lucee.admin.debug",driverNames)>
<cfset driverNames=ComponentListPackageAsStruct("debug",driverNames)>

<cfset drivers={}>
<cfloop collection="#driverNames#" index="n" item="fn">
	<cfif n EQ "Debug" or n EQ "Field" or n EQ "Group">
    	<cfcontinue>
    </cfif>
	<cfset tmp=createObject('component',fn)>
    <cfset drivers[trim(tmp.getId())]=tmp>
</cfloop>
<cfset driver=drivers["lucee-modern"]>
<cfset entry={}>
<cfloop query="entries">
	<cfif entries.type EQ "lucee-modern">
    	<cfset entry=querySlice(entries, entries.currentrow ,1)>
    </cfif>
</cfloop>

<cfif !isSimpleValue(log)>
	<cfset c=structKeyExists(entry,'custom')?entry.custom:{}>
	<cfset c.scopes=false>
	<cfset fun = "read#URL.tab#">
	<cfset "#driver[fun](c,log,"admin")#">
	<!--- <cfset driver.output(c,log,"admin")><cfelse>Data no longer available --->
</cfif>

<cfscript>
	function ComponentListPackageAsStruct(string package, cfcNames=structnew("linked")){
		try{
			local._cfcNames=ComponentListPackage(package);
			loop array="#_cfcNames#" index="i" item="el" {
				cfcNames[el]=package&"."&el;
			}
		}
		catch(e){}
		return cfcNames;
	}
</cfscript>