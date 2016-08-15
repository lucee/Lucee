<cfscript>
	/* Set an ID */
	id = 1;

	/* Fake a form */
	fakeForm = { 'fieldnames': '' };

	/* First check if our entity already exists - if not, make it */
	transaction {
		if( isNull( entityLoadByPK( 'RandomEntity', id ) ) ) {
			a = entityNew( 'RandomEntity' );
			a.setId( id );
			a.setName( 'Random Entity' );
			a.setToggle( true );
			entitySave( a );
		}
	}

	/*
		The following works on Lucee 4.5, but not on 5
	*/
	transaction {
		e = entityLoadByPK( 'RandomEntity', id );
		e.setName( 'After the Change' );
		if( structKeyExists( e, 'edit' ) ) {
			e.edit( fakeForm );
		}
		entitySave( e );
	}
	echo(entityLoadByPK( 'RandomEntity', 1 ).getId());
	// evaluateComponent("test.tickets.LDEV0921.RandomEntity","9eb2b797b17a72cb06b8f3feff559cf0",{},{"name":"After the Change","id":"1","toggle":false})
</cfscript>