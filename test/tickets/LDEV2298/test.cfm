<cfscript>
	param name="FORM.Scene" default="";
	param name="FORM.tableName" default="";

	if( FORM.scene == 1 OR  FORM.scene == 3){
		try{
			dsn = "ldev2298_DSN";
			queryExecute("
			 	UPDATE #form.tableName#
			 	SET emp_join_date = :utcNow
			 	WHERE id = :emp_id",{emp_id = 1,utcnow = '',cfsqltype = "cf_sql_timestamp"},{datasource = "ldev2298_DSN"}
			);
			result = queryExecute("
				SELECT *
				FROM #form.tableName#
				WHERE id = :emp_id",{emp_id = 1},{datasource = "ldev2298_DSN"}
			);
			writeoutput(result.emp_join_date);
		}catch(any e){
			writeoutput("Error");
		}
	}
	if( FORM.scene == 2 OR  FORM.scene == 4){
		try{
			dsn = "ldev2298_DSN";
			id = 1;
			utcNow = "";
			queryExecute(sql = "
				UPDATE #form.tableName#
				SET emp_join_date = :utcNow
				WHERE id = :id_num
				",
				params=[
					{name = "id_num", value = id, cfsqltype = "cf_sql_integer"},
					{name = "utcNow", value = utcNow, cfsqltype = "cf_sql_timestamp"}
				],
				options={
					datasource: dsn
				}
			);
			result = queryExecute(sql = "
				SELECT *
				FROM #form.tableName#
				WHERE id = :id_num
				",
				params = [
					{name = "id_num", value = id, cfsqltype = "cf_sql_integer"}
				],
				options={
					datasource: dsn
				}
			);
			writeoutput(result.emp_join_date);
		}catch (any e){
			writeoutput("Error");
		}
	}
</cfscript>