<cfscript>
	test = entityNew( "test4461" );
	test.setName( "LDEV4461" );
	test.setId( 1 );
	entitySave( test );

	param name="FORM.scene" default="";
	try {
		if (form.scene == 1) {
			res = EntityLoadByPk("test4461", 1);
			result = res.getId();
		} else if (form.scene == 2) {
			res = EntityLoadByPk(name = "test4461", id = 1 );
			result = res.getId();
		} else if (form.scene == "unique") {
			res = EntityLoadByPk("test4461", 1, true ); 
			result = res.getId();
		} else if (form.scene == "unique_named") {
			res = EntityLoadByPk(name="test4461", id=1, unique=true );
			result = res.getId();
		}
	} catch(any e) {
		result = e.stacktrace;
	}

	writeOutput(result);
</cfscript>