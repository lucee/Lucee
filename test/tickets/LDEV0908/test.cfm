<cfscript>
	Ormreload();
	msg="";
	try{
		obj = EntityNew("test",{col1:'test3',col2:'test4'});
		result = EntityLoadByExample(obj,true);
	} catch( any e){
		msg = e.message&"";
		systemOutput(e,1,1);
	}
	writeOutput(msg);
</cfscript>

