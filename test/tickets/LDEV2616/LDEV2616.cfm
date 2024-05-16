<cfscript>
	cfparam (name="form.scene",default="");
	if(form.scene eq 1){
		res = QueryExecute("SELECT * FROM LDEV2616 WHERE id IN (:id)",
			{ id:{ list :true, value :"" }},
         	{datasource ='LDEV2616'}
		)
		writeoutput(res.getresult().recordcount);
	}
	else if(form.scene eq 2){
		queryService = new query();
		queryService.setDatasource("LDEV2616");
		queryservice.addparam(name="id",value="");
		result = queryService.execute(sql="SELECT * FROM LDEV2616 WHERE id IN (:id)");
		writeoutput(result.getresult().recordcount);
	}
</cfscript>