<cfscript>
	param name="FORM.Scene" default="";
	param name="FORM.tableName" default="";
	param name="FORM.allowNull" default="false";
	param name="FORM.passDateParam" default="true";
	
	if (form.tableName neq "ldev2298_notnull"
			and form.tableName neq "ldev2298_null")
		throw "invalid tableName";

	if( FORM.scene == 1 OR  FORM.scene == 3){
		try{
			dsn = "ldev2298_DSN";

			params = {emp_id = 1};
			if (form.passDateParam)
				params.utcnow = '';

			update_result = queryExecute("
			 	UPDATE 	#form.tableName#
			 	SET 	emp_join_date = :utcNow
			 	WHERE 	id = :emp_id",
				params,
				{datasource = dsn}
			);
			result = queryExecute("
				SELECT 	*
				FROM 	#form.tableName#
				WHERE 	id = :emp_id",
				{emp_id = 1},
				{datasource = dsn}
			);
			writeoutput(result.emp_join_date);
		}catch(any e){
			writeoutput(e.stacktrace);
		}
	}
	if( FORM.scene == 2 OR  FORM.scene == 4){
		try{
			dsn = "ldev2298_DSN";
			id = 1;
			utcNow = "";

			params = [
				{name="id_num", value=id, cfsqltype="cf_sql_integer"}
			];
			if (form.passDateParam)
				arrayAppend(params, {name="utcNow", value=utcNow, cfsqltype="cf_sql_timestamp", null=form.allowNull});

			update_result = queryExecute(sql = "
				UPDATE 	#form.tableName#
				SET 	emp_join_date = :utcNow
				WHERE 	id = :id_num
				",
				params=params,
				options={
					datasource: dsn
				}
			);
			result = queryExecute(sql = "
				SELECT *
				FROM	#form.tableName#
				WHERE 	id = :id_num
				",
				params = [
					{name="id_num", value=id, cfsqltype="cf_sql_integer"}
				],
				options={
					datasource: dsn
				}
			);
			writeoutput(result.emp_join_date);
		}catch (any e){
			writeoutput(e.stacktrace);
		}
	}
</cfscript>
