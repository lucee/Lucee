<cfset pool['HEAP']="Heap">
<cfset pool['NON_HEAP']="Non-Heap">

<cffunction name="printMemory" returntype="struct">
	<cfargument name="usage" type="query" required="yes">
	<cfargument name="showTitle" type="boolean" default="true" required="false">
	<cfset var used=evaluate(ValueList(arguments.usage.used,'+'))>
	<cfset var max=evaluate(ValueList(arguments.usage.max,'+'))>
	<cfset var init=evaluate(ValueList(arguments.usage.init,'+'))>
	<cfset var qry=QueryNew(arguments.usage.columnlist)>
	<cfset QueryAddRow(qry)>
    <cfset QuerySetCell(qry,"type",arguments.usage.type)>
    <cfset QuerySetCell(qry,"name",variables.pool[arguments.usage.type])>
    <cfset QuerySetCell(qry,"init",init,qry.recordcount)>
    <cfset QuerySetCell(qry,"max",max,qry.recordcount)>
    <cfset QuerySetCell(qry,"used",used,qry.recordcount)>
    <cfset arguments.usage=qry>
		<cfif arguments.showTitle><b>#pool[usage.type]#</b></cfif>
		<cfset var str = {}>
		<cfloop query="arguments.usage">
			<cfset str.pused=int(100/arguments.usage.max*arguments.usage.used)>
			<cfset str.pused =(str.pused GT 100)?100:(str.pused LT 0)?0:str.pused>
   			<cfset str.pfree=100-str.pused>
		</cfloop>
		<cfreturn str>
</cffunction>

<cffunction name="sysMetric" returnType="struct" access="remote" localmode="modern">
	<cfset systemInfo=GetSystemMetrics()>
	<cfset heap = variables.printMemory(getmemoryUsage("heap"),false)>
	<cfset nonHeap = variables.printMemory(getmemoryUsage("non_heap"),false)>
	<cfset cpuSystemData = int((systemInfo.cpuSystem ?: 0) *100)>
	<cfset  cpuProcessData= int((systemInfo.cpuProcess ?: 0) *100)>
	<cfset result = {
		"heap":heap.pused,
		"nonheap":nonHeap.pused,
		"cpuSystem": (cpuSystemData GT 100) ? 100 :cpuSystemData,
		"cpuProcess": (cpuProcessData GT 100) ? 100 :cpuProcessData
	}>
	<cfreturn result>
</cffunction>

