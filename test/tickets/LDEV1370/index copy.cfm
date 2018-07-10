<cfsetting showdebugoutput="false"><cfscript>
test = entityLoadByPK('Test', 1);
if(isNull(test)) {
	test =	entityNew("Test");
	test.setId(1);
	test.setTitle("inital title");
	entitySave(test);
	ormFlush();

	test = entityLoadByPK('Test', 1);
}
	qry=queryExecute('select * from Test1370');
	echo("inital:"&qry.recordcount&":"&qry.id&";");

	// now we make a change and flush it explicitly
	test = entityLoadByPK('Test', 1);
	test.setTitle("explicit change");
	entitySave(test);
	ormFlush();
	qry=queryExecute('select * from Test1370');
	echo("explicit:"&qry.recordcount&":"&qry.id&":"&qry.title&";");


	// now we make a change in a transaction
	transaction {
		test = entityLoadByPK('Test', 1);
		test.setTitle("transaction change");
		entitySave(test);
	}
	qry=queryExecute('select * from Test1370');
	echo("transaction:"&qry.recordcount&":"&qry.id&":"&qry.title&";");
	

</cfscript>