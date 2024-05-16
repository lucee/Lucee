<cfscript>
	param name="form.scene" default="";

	if( form.scene EQ 1 ){

		usingQMarkwithdb = queryExecute(
			sql = "SELECT stu_name from LDEV2754 WHERE id = /*MAY BREAK ON EXTRA PAYMENT AND SINGLES?*/ :id",
			params = {
				id : { value : 1, type : "integer" }
			},
			queryoptions = { datasource = "ldev2754_dsn" }
		);
		writeOutput(usingQMarkwithdb.stu_name);
	}

	if( form.scene EQ 2 ){

		usingApostrophewithdb = queryExecute(
			sql = "SELECT stu_name FROM ldev2754 WHERE id = /* this comment's will break params */ :id",
			params = {
				id : { value : 1, type : "integer" }
			},
			queryoptions = { datasource = "ldev2754_dsn" }
		);
		writeOutput(usingApostrophewithdb.stu_name);
	}

	if( form.scene EQ 3 ){

		qNew = queryNew( "id,name", "integer,varchar", [ { id = 1, name = "test" }, { id = 2, name = "lucee" } ] );
		usingQOQ = queryExecute(
			sql = "SELECT name FROM qNew WHERE id = /* this comment's will break params */ :id",
			params = {
				id : { value : 2, type : "integer" }
			},
			queryoptions = { dbtype = "query" }
		);
		writeOutput(usingQOQ.name);
	}

</cfscript>