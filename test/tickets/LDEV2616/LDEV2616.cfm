<cfscript>
	res = QueryExecute("SELECT * FROM LDEV2616 WHERE id IN (:id)",
		{ id:{ list :true, value :"" }},
		{datasource ='LDEV2616'}
	)
	writeoutput(res.getresult().recordcount);
</cfscript>