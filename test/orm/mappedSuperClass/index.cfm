<cfscript>

	try {
		// Force ORM reload
		ormReload();

		// Test 1: Create entity instance
		person = new Person();

		// Test 2: Verify inherited properties are accessible via accessors
		person.setId( 1 );
		person.setCreatedDate( now() );
		person.setModifiedDate( now() );
		person.setFirstName( "John" );
		person.setLastName( "Doe" );

		if ( person.getId() != 1 ) {
			throw( "Test 2 FAILED: getId() returned '#person.getId()#', expected 1" );
		}
		if ( person.getFirstName() != "John" ) {
			throw( "Test 2 FAILED: getFirstName() returned '#person.getFirstName()#', expected 'John'" );
		}

		// Test 3: Verify properties are in component metadata
		md = getComponentMetadata( person );
		props = md.properties;
		propNames = [];
		for ( prop in props ) {
			arrayAppend( propNames, prop.name );
		}

		if ( !arrayFind( propNames, "id" ) ) {
			throw( "Test 3 FAILED: 'id' property not found in metadata" );
		}
		if ( !arrayFind( propNames, "createdDate" ) ) {
			throw( "Test 3 FAILED: 'createdDate' property not found in metadata" );
		}
		if ( !arrayFind( propNames, "modifiedDate" ) ) {
			throw( "Test 3 FAILED: 'modifiedDate' property not found in metadata" );
		}
		if ( !arrayFind( propNames, "firstName" ) ) {
			throw( "Test 3 FAILED: 'firstName' property not found in metadata" );
		}

		// Test 4: Verify persistent="false" override on inherited property
		modifiedDateProp = {};
		for ( prop in props ) {
			if ( prop.name == "modifiedDate" ) {
				modifiedDateProp = prop;
				break;
			}
		}
		if ( structIsEmpty( modifiedDateProp ) ) {
			throw( "Test 4 FAILED: modifiedDate property not found" );
		}
		if ( !structKeyExists( modifiedDateProp, "persistent" ) || modifiedDateProp.persistent != false ) {
			throw( "Test 4 FAILED: modifiedDate should have persistent=false, got: #serializeJSON( modifiedDateProp )#" );
		}

		// Test 5: Save and load entity (verifies DB schema doesn't include modifiedDate column)
		entitySave( person );
		ormFlush();

		ormClearSession();
		loaded = entityLoadByPK( "Person", 1 );
		if ( isNull( loaded ) ) {
			throw( "Test 6 FAILED: Entity not found after save" );
		}
		if ( loaded.getFirstName() != "John" ) {
			throw( "Test 6 FAILED: Loaded entity has wrong firstName: '#loaded.getFirstName()#'" );
		}

		// All tests passed - output nothing

	}
	catch ( any e ) {
		writeOutput( e.message );
		rethrow;
	}
</cfscript>
