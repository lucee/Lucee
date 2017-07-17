<cfscript>
	/* Set an ID */
	id = 1;

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

	result = OrmExecuteQuery( "FROM RandomEntity where name=:name",{name='Random Entity'}, true );

	echo(serialize(result.getName()));

	//echo(entityLoadByPK( 'RandomEntity', 1 ).getId());
</cfscript>