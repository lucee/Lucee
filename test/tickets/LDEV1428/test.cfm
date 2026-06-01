<cfscript>
	param name="form.scene" default="1";
	function testDelete(){
		// Create test record to delete
		var user = entityNew( "ActiveUser" );
		user.setFirstName( 'unitTest' );
		user.setLastName( 'unitTest' );
		user.setUsername( 'unitTest' );
		user.setPassword( 'unitTest' );
		entitySave( user );
		ORMFlush();

		try{
			entityDelete(user);

			ORMFlush();
			// Clear the session just in case to make sure we try and load the deleted entity
			ORMClearSession();

			var testUser = entityLoad( "ActiveUser", { firstName="unitTest" } , true );
			echo( "#isNull( testUser )#|" );
		}
		catch(any e){
			//debug( testUser );
			echo( e.detail & e.message );
		}
		finally{
			var q = new Query( );
			q.execute( sql="delete from users1428 where firstName = 'unitTest'" );
		}
	}

	function testDeleteByID(){
		// Create test record to delete
		var user = entityNew( "ActiveUser" );
		user.setFirstName( 'unitTest' );
		user.setLastName( 'unitTest' );
		user.setUsername( 'unitTest' );
		user.setPassword( 'unitTest' );
		entitySave( user );
		ORMFlush();

		try{
			// Hibernate 7.3+ HQL is strictly typed — `id` is ormtype="int", so use a bind param rather than a quoted string literal
			ORMExecuteQuery("delete from ActiveUser where id = :id", { id: user.getID() });
			ORMFlush();

			// Clear the session just in case to make sure we try and load the deleted entity
			ORMClearSession();
			// Try to load
			var testUser = entityLoad( "ActiveUser", { firstName="unitTest" } , true );
			echo( "#isNull( testUser )#|" );
		}
		catch(any e){
			//debug( testUser );
			echo( e.detail & e.message );
		}
		finally{
			var q = new Query( );
			q.execute( sql="delete from users1428 where firstName = 'unitTest'" );
		}
	}

	function testDeleteWhere(){
		for(var x=1; x lte 3; x++){
			var user = entityNew("ActiveUser");
			user.setFirstName('unitTest#x#');
			user.setLastName('unitTest');
			user.setUsername('unitTest');
			user.setPassword('unitTest');
			entitySave(user);
		}
		ORMFlush();
		var q = new Query();

		try{
			if( structKeyExists( server, "lucee" ) ){ ORMCloseSession(); }
			// Hibernate 7.3+ HQL is case-sensitive on identifiers — must match the CFC property declaration `UserName`
			ORMExecuteQuery("delete from ActiveUser where UserName = 'unitTest'");
			ORMFlush();
			ORMClearSession();

			var result = q.execute(sql="select * from users1428 where userName = 'unitTest'");
			echo("#result.getResult().recordcount#|" );
		}
		catch(any e){
			echo(e.detail & e.message & e.stackTrace);
		}
		finally{
			q.execute(sql="delete from users1428 where userName = 'unitTest'");
		}
	}


	if(form.scene EQ 1)testDelete();
	else if(form.scene EQ 2)testDeleteByID();
	else if(form.scene EQ 3)testDeleteWhere();
	else if(form.scene EQ 4){
		testDelete();
		testDeleteByID();
		testDeleteWhere();
	}
</cfscript>