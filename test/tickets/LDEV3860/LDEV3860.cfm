<cfscript>
	param name="FORM.scene" default="";
	hasError = false;
	try {
		if (form.scene == 1) {
			transaction {
				ormGetSession();
				writeDump(foo); // variable [FOO] doesn't exist
			}
		} else if (form.scene == 2) {
			obj = EntityNew( "invalid_entity_name",{name:'test1',givenName:'test2'} );
		} else if (form.scene == 3) {
			transaction {
				obj = EntityNew( "invalid_entity_name", {name:'test3',givenName:'test4'} );
			}
		} else if (form.scene == 4) {
			transaction {
				writeDump(foo); // variable [FOO] doesn't exist
				entityload( "test" ); // any ORM stuff
			}
		} else if (form.scene == 5) {
			transaction {
				entityload( "test" ); // any ORM stuff
				writeDump(foo); // variable [FOO] doesn't exist
			}
		} else if (form.scene == 6) {
			transaction {
				entitynew( "test" ); // any ORM stuff
				queryExecute( "SELECT * FROM testLDEV3680" ); // datasource query
				writeDump(foo); // variable [FOO] doesn't exist
			}
		} else if (form.scene == 7) {
			transaction {
				ormGetSession(); // ormGetSession()
				queryExecute( "SELECT name FROM testLDEV3680" ); // datasource query
				writeDump(foo); // variable [FOO] doesn't exist
			}
		}
	} catch ( e ){
		echo( e.message );
		hasError = true;
	}
	if ( !hasError )
		echo( "true" );
</cfscript>