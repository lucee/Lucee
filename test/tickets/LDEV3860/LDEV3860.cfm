<cfscript>
	param name="FORM.scene" default="";

	hasError = false;
	try {
		if (form.scene == 1) {
			transaction {
				ormGetSession();
			}
		} else if (form.scene == 2) {
			obj = EntityNew( "invalid_entity_name",{name:'test1',givenName:'test2'} );
		} else if (form.scene == 3) {
			transaction {
				obj = EntityNew( "invalid_entity_name", {name:'test3',givenName:'test4'} );
			}
		} else if (form.scene == 4) {
			transaction {
				entityload( "person" ); // any ORM stuff
			}
		} else if (form.scene == 5) {
			transaction {
				entityload( "person" ); // any ORM stuff
			}
		} else if (form.scene == 6) {
			transaction {
				entitynew( "person" ); // any ORM stuff
				queryExecute( "SELECT * FROM persons" ); // datasource query
			}
		} else if (form.scene == 7) {
			transaction {
				ormGetSession(); // ormGetSession()
				queryExecute( "SELECT name FROM Persons" ); // datasource query
			}
		}
	} catch ( e ){
		echo( e.message );
		hasError = true;
	}
	if ( !hasError )
		echo( "true" );
</cfscript>