<cffunction name="printMemory" returntype="struct">
	<cfargument name="usage" type="query" required="yes">
	<cfargument name="showTitle" type="boolean" default="true" required="false">
	<cfset var used=evaluate(ValueList(arguments.usage.used,'+'))>
	<cfset var max=evaluate(ValueList(arguments.usage.max,'+'))>
	<cfset var init=evaluate(ValueList(arguments.usage.init,'+'))>
	<cfset var qry=QueryNew(arguments.usage.columnlist)>
	<cfset QueryAddRow(qry)>
    <cfset QuerySetCell(qry,"type",arguments.usage.type)>
    <cfset QuerySetCell(qry,"name",stText.Overview.pool[arguments.usage.type])>
    <cfset QuerySetCell(qry,"init",init,qry.recordcount)>
    <cfset QuerySetCell(qry,"max",max,qry.recordcount)>
    <cfset QuerySetCell(qry,"used",used,qry.recordcount)>
    <cfset arguments.usage=qry>
		<cfif arguments.showTitle><b>#stText.Overview.pool[usage.type]#</b></cfif>
		<cfset var str = {}>
		<cfloop query="arguments.usage">
			<cfset str.pused=int(100/arguments.usage.max*arguments.usage.used)>
			<cfset str.pused =(str.pused GT 100)?100:(str.pused LT 0)?0:str.pused>
   			<cfset str.pfree=100-str.pused>
			<cfset str.used=int(used/1024/1024)>
			<cfset str.max=int(max/1024/1024)>
		</cfloop>
		<cfreturn str>
</cffunction>

<cffunction name="sysMetric" returnType="struct" access="remote" localmode="modern">
	<cfset systemInfo=GetSystemMetrics()>
	<cfset heap = variables.printMemory(getmemoryUsage("HEAP"),false)>
	<cfset nonHeap = variables.printMemory(getmemoryUsage("NON_HEAP"),false)>
	<cfset cpuSystemData = int((systemInfo.cpuSystem ?: 0) *100)>
	<cfset  cpuProcessData= int((systemInfo.cpuProcess ?: 0) *100)>
	<cfset result = {
		"heap":heap,
		"nonheap":nonHeap,
		"cpuSystem": (cpuSystemData GT 100) ? 100 :cpuSystemData,
		"cpuProcess": (cpuProcessData GT 100) ? 100 :cpuProcessData
	}>
	<cfreturn result>
</cffunction>

