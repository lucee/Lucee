<cfif request.admintype EQ "server"><cflocation url="#request.self#" addtoken="no"></cfif>

<cfset error.message="">
<cfset error.detail="">
<cfparam name="url.action2" default="list">
<cfparam name="form.mainAction" default="none">
<cfparam name="form.subAction" default="none">

<cfadmin 
	action="getGatewayEntries"
	type="#request.adminType#"
	password="#session["password"&request.adminType]#"
	returnVariable="entries">
    
<cfadmin 
	action="securityManager"
	type="#request.adminType#"
	password="#session["password"&request.adminType]#"
	returnVariable="access"
	secType="gateway">
    
	



<!--- load available drivers --->
<cfset variables.drivers={}>
<cfset driverNames=structnew("linked")>
<cfset driverNames=ComponentListPackageAsStruct("lucee-server.admin.gdriver",driverNames)>
<cfset driverNames=ComponentListPackageAsStruct("lucee.admin.gdriver",driverNames)>
<cfset driverNames=ComponentListPackageAsStruct("gdriver",driverNames)>

<cfloop collection="#driverNames#" index="n" item="fn">
	
	<cfif n NEQ "Gateway" and n NEQ "Field" and n NEQ "Group">
		<cfset tmp = createObject("component",fn)>
		<cfset drivers[n]=tmp>
	</cfif>
</cfloop>
	
<!--- add driver to query --->
<cfset QueryAddColumn(entries,"driver",array())>
<cfloop query="entries">
    <cfloop collection="#drivers#" index="key" item="d">
    	<cfif 
			(StructKeyExists(d,'getCFCPath')?d.getCFCPath() EQ entries.cfcPath:"" EQ entries.cfcPath)
			and 
			(StructKeyExists(d,'getClass')?d.getClass() EQ entries.class:"" EQ entries.class)
			>
			<cfset QuerySetCell(entries,"driver",d,entries.currentrow)>
            
		</cfif>
    </cfloop>
    
</cfloop>


<cfswitch expression="#url.action2#">
	<cfcase value="list"><cfinclude template="services.gateway.list.cfm"/></cfcase>
	<cfcase value="create"><cfinclude template="services.gateway.create.cfm"/></cfcase>

</cfswitch>