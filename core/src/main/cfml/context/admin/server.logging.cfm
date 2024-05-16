<cfset error.message="">
<cfset error.detail="">
<cfparam name="url.action2" default="list">
<cfparam name="form.mainAction" default="none">
<cfparam name="form.subAction" default="none">	
	
<cfadmin 
        action="getLogSettings" 
        type="#request.adminType#"
        password="#session["password"&request.adminType]#"
        
        returnVariable="logs"
        remoteClients="#request.getRemoteClients()#">
<cfscript>
	function doSortedStruct(arr) {
		arraySort(arguments.arr,function(l,r) {
			return compare(arguments.l.getLabel(),arguments.r.getLabel());
		});
		var sct=structNew('ordered');
		loop array=arguments.arr item="local.el" {
			sct[el.getClass()]=el;
		} 
		return sct;
	}
</cfscript>
<!--- load available appenders --->
<cfset arr=[]>
<cfset names=structnew("linked")>
<cfset names=ComponentListPackageAsStruct("lucee-server.admin.logging.appender",names)>
<cfset names=ComponentListPackageAsStruct("lucee.admin.logging.appender",names)>
<cfset names=ComponentListPackageAsStruct("logging.appender",names)>
<cfloop collection="#names#" index="n" item="fn">
	<cfif n NEQ "Appender" and n NEQ "Field" and n NEQ "Group">
		<cfset arrayAppend(arr,createObject("component",fn))>
	</cfif>
</cfloop>
<cfset appenders=doSortedStruct(arr)>

<!--- load available layouts --->
<cfset arr=[]>
<cfset names=structnew("linked")>
<cfset names=ComponentListPackageAsStruct("lucee-server.admin.logging.layout",names)>
<cfset names=ComponentListPackageAsStruct("lucee.admin.logging.layout",names)>
<cfset names=ComponentListPackageAsStruct("logging.layout",names)>
<cfloop collection="#names#" index="n" item="fn">
	<cfif n NEQ "Layout" and n NEQ "Field" and n NEQ "Group">
		<cfset arrayAppend(arr,createObject("component",fn))>
	</cfif>
</cfloop>
<cfset layouts=doSortedStruct(arr)>


<cfset access=true>
<!--- TODO
<cfadmin 
	action="securityManager"
	type="#request.adminType#"
	password="#session["password"&request.adminType]#"
	returnVariable="access"
	secType="logging">
	 --->
	

<cfswitch expression="#url.action2#">
	<cfcase value="list"><cfinclude template="server.logging.list.cfm"/></cfcase>
	<cfcase value="create"><cfinclude template="server.logging.create.cfm"/></cfcase>

</cfswitch>