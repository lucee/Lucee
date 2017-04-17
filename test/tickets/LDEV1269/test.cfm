<cfscript>
	error='';
	test = new query( sql="SELECT * FROM test", datasource="sample" ).execute().getResult();
	result1 = new Query(sql = "SELECT * FROM test", dbtype = "query", test = test).execute().getResult();
	try{
		result2 = new Query(sql = "SELECT COUNT(*) AS cnt FROM test", dbtype = "query", test = test).execute().getResult();
		result3=result2.RecordCount;
	}catch( any e ){
		if( len(e.Detail) ){
			result3 = e.Detail;
		}else{
			result3 = e.Message;
		}
	}
</cfscript>
<cfoutput>#result3#</cfoutput>