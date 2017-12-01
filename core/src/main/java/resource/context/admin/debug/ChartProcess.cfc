<cfcomponent output="true">

	<cffunction name="printMemory" returntype="struct">
		<cfset pool['HEAP']="Heap">
		<cfset pool['NON_HEAP']="Non-Heap">
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
			<cfset str = {}>
			<cfloop query="usage">
				<cfset str.pused=int(100/arguments.usage.max*arguments.usage.used)>
	   			<cfset str.pfree=100-str.pused>
			</cfloop>
			<cfreturn str>
	</cffunction>


	<cffunction name="sysMetric" returnType="struct" returnformat="JSON" access="remote">
		<cfset systemInfo=GetSystemMetrics()>
		<cfset heap = printMemory(getmemoryUsage("heap"),false)>
		<cfset nonHeap = printMemory(getmemoryUsage("non_heap"),false)>
		<cfset result = {
			"heap":heap.pused ?: 0,
			"nonheap":nonHeap.pused ?: 0,
			"cpuSystem": int((systemInfo.cpuSystem ?: 0) * 100),
			"cpuProcess": int((systemInfo.cpuProcess ?: 0) *100)
		}>
		<cfreturn result>
	</cffunction>
</cfcomponent>

