<cfscript>
	param name="FORM.scene" default="";
	
	if(form.scene eq 1){
		qQuery = QueryExecute(
			"SELECT 1 WHERE 1E-8 = :FloatingPoint", 
			{ 
				FloatingPoint = { 
					cfsqltype="CF_SQL_FLOAT", 
					value="0.00000001"
				}
			},
			{
				datasource="luceedb"
			}
		);
		writeOutput(qQuery.recordcount);
	}
	 
	if(form.scene eq 2){

		qQuery1 = QueryExecute(
			"SELECT 1 WHERE 1E-8 = :FloatingPoint", 
			{
				FloatingPoint = { 
					cfsqltype="CF_SQL_NUMERIC", 
					value="0.00000001"
				} 
			},
			{
				datasource="luceedb"
			}
		);
		writeOutput(qQuery1.recordcount);
	}
</cfscript>




