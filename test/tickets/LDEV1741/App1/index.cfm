<cfscript>
	//ormReload();

	transaction {
		e=entityNew("foo");
		e.setLabel("Bar");
		e.setId("1");
		entitySave(e);
		transactionCommit();
	}	
	//ormReload();

	obj = entityLoad("Foo");
	writeOutput(obj[1].getLabel());
</cfscript>
