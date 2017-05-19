<cfscript>
	try{
		testQry = queryNew("name,age,dept","varchar,integer,varchar", [['saravana',35,'MD'],['Bob',20, 'Employee'],['pothys',25, 'Employee']]);
		result = arrayToList(testQry.getMeta().getcolumnlabels());
	}catch( any e ){
		result = e.message;
	}
	writeOutput(result);
</cfscript>
