<cfscript>
	param name="FORM.scene" default="";
	if(form.scene eq 1){
		queryExecute( "INSERT INTO test_pgSQL ( id,name ) VALUES ( :id, :name )", { 
			id = { value = 1, cfsqltype = "integer" },
			name = { value="lucee", cfsqltype="varchar" } 
		}, { 
			result="result" 
		} );
		writeOutput( result.generatedKey );
	}
	if(form.scene eq 2){
		queryExecute( "INSERT INTO test_pgSQL ( id,name,age ) VALUES ( :id, :name, :age )", { 
			id = { value = 2, cfsqltype = "integer" },
			name = { value="testcase", cfsqltype="varchar" }, 
			age = { value=31, cfsqltype="integer"} 
		}, { 
			result="resultOne" 
		} );
		writeOutput( resultOne.generatedKey );
	}
</cfscript>
