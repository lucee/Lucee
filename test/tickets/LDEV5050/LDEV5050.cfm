<cfscript>
	queryService = new query();
	queryService.setDatasource("LDEV5050");
	queryservice.addparam( name="id", value="", list=true);
	result = queryService.execute(sql="SELECT * FROM LDEV5050 WHERE id IN (:id)");
	systemOutput(result.getresult(), true);
	systemOutput(result, true);
	writeoutput(result.getresult().recordcount);	
</cfscript>