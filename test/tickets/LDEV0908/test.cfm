<cfscript>
	Ormreload();
	try{
		newThing = EntityNew("test",{col1:'test3',col2:'test4'});
		checkForDupes = EntityLoadByExample(newThing,true);
	} catch( any e){
		checkForDupes = e.message;
	}
	writeOutput(isNull(checkForDupes));
</cfscript>

