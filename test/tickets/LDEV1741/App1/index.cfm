<cfscript>
	//ormReload();

	transaction {
		e=entityNew("foo");
		e.setLabel("Bar");
		e.setId("1");
		entitySave(e);
		transactionCommit();
	}	
	ormReload();

	obj = entityLoad("Foo");
	systemOutput(obj,1,1);
	systemOutput(EntityNameArray(),1,1);

	writeOutput(obj[1].getLabel());
</cfscript>
