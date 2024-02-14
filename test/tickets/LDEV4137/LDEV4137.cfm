<cfscript>
	param name="FORM.scene" default="";
	value = 100000000000000000000000000000000000000000;
	if(form.scene == 1) {
		try {
			query name="q" {
				echo("select #value# as num");
			}
			writeOutput(q.num);
		}
		catch(any e) {
			writeoutput("Throws error for large number");
		}
	}
	else if(form.scene == 2) {
		try {
			query name="q" {
				echo("select ") 
				queryParam cfsqltype="cf_sql_numeric" value="#value#";
				echo(" as num") 
			}
			writeOutput(q.num);
		}
		catch(any e) {
			writeoutput("Throws error for large number with queryparam");
		}
	}
</cfscript>