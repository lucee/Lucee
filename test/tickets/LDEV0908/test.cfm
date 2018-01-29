<cfscript>
	Ormreload();
	try{
		obj = EntityNew("test",{col1:'test3',col2:'test4'});
		result = EntityLoadByExample(obj,true);
	} catch( any e){
		result = e.message;
	}
	writeOutput(isNull(result));
</cfscript>

