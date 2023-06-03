<cfscript>
	test = entityNew( "test4461" );
	test.setName( "LDEV4461" );
	test.setId( 1 );
	entitySave( test );

	param name="FORM.scene" default="";

	if (form.scene == 1) {
		try {
			res = EntityLoadByPk("test4461", 1);
			result = res.getId();
			}
			catch(any e) {
				result = e.message;
			}
	}
	else if (form.scene == 2) {
		try {
			res = EntityLoadByPk(name = "test4461", id = 1 );
			result = res.getId();
		} 
		catch(any e) {
			result = e.message;
		}
	}

	writeOutput(result);
</cfscript>