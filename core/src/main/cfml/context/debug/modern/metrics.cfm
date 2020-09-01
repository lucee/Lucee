<cfscript>
setting showdebugoutput="false";

pool['HEAP']="Heap";
pool['NON_HEAP']="Non-Heap";


struct function printMemory(required query usage){
	var used=evaluate(ValueList(arguments.usage.used,'+'));
	var max=evaluate(ValueList(arguments.usage.max,'+'));
	var init=evaluate(ValueList(arguments.usage.init,'+'));
	var qry=QueryNew(arguments.usage.columnlist);
	QueryAddRow(qry);
	QuerySetCell(qry,"type",arguments.usage.type);
	QuerySetCell(qry,"name",variables.pool[arguments.usage.type]);
	QuerySetCell(qry,"init",init,qry.recordcount);
	QuerySetCell(qry,"max",max,qry.recordcount);
	QuerySetCell(qry,"used",used,qry.recordcount);
	arguments.usage=qry;
	var str = {};
	loop query=arguments.usage {
		str.pused=int(100/arguments.usage.max*arguments.usage.used);
		str.pused =(str.pused GT 100)?100:(str.pused LT 0)?0:str.pused;
		str.pfree=100-str.pused;
	}
	return str;
}
	
struct function sysMetric()  localmode="modern" {
	systemInfo=GetSystemMetrics();
	heap = variables.printMemory(getmemoryUsage("heap"));
	nonHeap = variables.printMemory(getmemoryUsage("non_heap"));
	cpuSystemData = int((systemInfo.cpuSystem ?: 0) *100);
	cpuProcessData= int((systemInfo.cpuProcess ?: 0) *100);
	result = {
		"heap":heap.pused,
		"nonheap":nonHeap.pused,
		"cpuSystem": (cpuSystemData GT 100) ? 100 :cpuSystemData,
		"cpuProcess": (cpuProcessData GT 100) ? 100 :cpuProcessData
	};
	return result;
}



struct = sysMetric();
content type="application/json";
echo(SerializeJSON(struct));
</cfscript>