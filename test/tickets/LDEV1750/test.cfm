<cfscript>

	variables.qry = queryNew(
		 "name, version"
		,"varchar, varchar"
		,[
			 ["ACF",  "2016"]
			,["Railo", "4.2"]
			,["Lucee", "5.3"]
		]
	);

	var exception = nullValue();

	try {
		// this should throw an exception
		query name="local.q2" dbtype="query";
	}
	catch (ex){
		echo(ex.type);
	}

</cfscript>