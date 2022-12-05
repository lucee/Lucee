<cfscript>
	param name="FORM.scene" default="";
	param name="FORM.params" default="";

	sql = form.scene == "named"? "SELECT name FROM LDEV4044 WHERE = :id" : "SELECT name FROM LDEV4044 WHERE = ?";

	try {
		if (form.params == "empty Params" ) {
			queryExecute(
				sql="#sql#"
				, params={} // defined empty struct as params
				);
		}
		else if (form.params == "without params") {
			queryExecute(sql="#sql#");
		}
	}
	catch (any e) {
		writeoutput("JDBC query with #form.scene# parameter and #form.params# failed as expected");
	}

</cfscript>