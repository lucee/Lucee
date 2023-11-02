<cfscript>
	param name="form.scene" default="";
    query = new Query(sql:"INSERT INTO LDEV2819 VALUES (8,2);SELECT SCOPE_IDENTITY() AS [SCOPE_IDENTITY];");
    query.setDatasource('LDEV2819_DSN');
   	result = query.execute().getresult();
   	if(form.scene eq 1){
	  	writeOutput(isNull(result));
   	}
   	if(form.scene eq 2){
		writeOutput(result.SCOPE_IDENTITY);
  	}
</cfscript>