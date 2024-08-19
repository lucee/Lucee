<cfscript>
	param name="FORM.scene" default="";

	if (form.scene eq "CF_SQL_FLOAT"){
		qQuery = QueryExecute(
			"SELECT 1 WHERE 1E-8 = :FloatingPoint",
			{
				FloatingPoint = {
					cfsqltype="CF_SQL_FLOAT",
					value="0.00000001"
				}
			},
			{
				datasource="ldev2403",
				result="result"
			}
		);
		writeOutput(qQuery.recordcount);
	} else if (form.scene eq "CF_SQL_NUMERIC"){

		qQuery1 = QueryExecute(
			"SELECT 1 WHERE 1E-8 = :FloatingPoint",
			{
				FloatingPoint = {
					cfsqltype="CF_SQL_NUMERIC",
					value="0.00000001"
				}
			},
			{
				datasource="ldev2403",
				result: "result"
			}
		);
		writeOutput(qQuery1.recordcount);
	}
</cfscript>




