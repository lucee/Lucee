<cfsetting showdebugoutput="false">
<cfadmin
	action="getLoggedDebugData"
	type="web"
	returnVariable="all">
<cfset listIds = "">
<cfloop array="#all#" index="i">
	<cfset listIds = listAppend(listIds, i.id)>
</cfloop>
<cfif isNull(url.id)>
	<cfset url.id=all[arrayLen(all)].id>
<cfelse>
	<cfif listFind(listIds, url.id) EQ 0>
		<cfset url.id=all[arrayLen(all)].id>
	</cfif>
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
	<cfif n EQ "Debug" or n EQ "Field" or n EQ "Group" or n EQ "ChartProcess">
    	<cfcontinue>
    </cfif>
	<cfset tmp=createObject('component',fn)>
    <cfset drivers[trim(tmp.getId())]=tmp>
</cfloop>
<cfset driver=drivers["lucee-modern-extended"]>
<cfset entry={}>
<cfloop query="entries">
	<cfif entries.type EQ "lucee-modern-extended">
    	<cfset entry=querySlice(entries, entries.currentrow ,1)>
    </cfif>
</cfloop>

<cfif !isSimpleValue(log)>
	<cfset c=structKeyExists(entry,'custom')?entry.custom:{}>
	<cfset c.scopes=false>
	<cfset fun = "read#URL.tab#">
	<cfset "#driver[fun](c,log,"admin")#">
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