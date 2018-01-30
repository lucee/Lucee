<cfscript>
	foo_id = '1234';
	if ( application.applicationname eq 'myAppTwo' ) {
		ormReload();
	}
	try{
		foo = entityLoadByPK('Foo',foo_id);
		if ( isNull(foo) ) {
			transaction {
				foo		= entityNew( 'Foo' );
				foo.setID(foo_id);
				foo.setLabel('Goodbye World!');
				entitySave(foo);
				transactionCommit();
			}
		}
		obj = entityLoadByPK('Foo',foo_id);
		result = obj.getId();
	} catch ( any e ){
		result = e.message;
	}
	writeOutput(result);
</cfscript>
