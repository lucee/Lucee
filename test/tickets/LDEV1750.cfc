<cfcomponent extends="org.lucee.cfml.test.LuceeTestCase">


<cfscript>
	
	public function beforeAll(){
		variables.qry = queryNew(
			 "name, version"
			,"varchar, varchar"
			,[
				 ["ACF",  "2016"]
				,["Railo", "4.2"]
				,["Lucee", "5.3"]
			]
		);
	}

	public void function testSqlBody(){

		query name="local.q2" dbtype="query" {
			echo("select * from variables.qry");
		}
	}

	public void function testSqlAttr(){

		var sql = "select * from variables.qry";

		query name="local.q2" dbtype="query" sql=sql {};
	}

	public void function testSqlAttrNobody(){

		var sql = "select * from variables.qry";

		query name="local.q2" dbtype="query" sql=sql;
	}

	public void function testSqlMissing(){

		var exception = nullValue();

		try {
			// this should throw an exception
			query name="local.q2" dbtype="query";
		}
		catch (ex){
			exception = ex;
		}

		if (isNull(exception))
			throw object=exception;
	}

</cfscript>
	
	<cffunction name="testSqlAttrNobodyTag">
		<cfquery 
			name="local.q2" 
			dbtype="query" 
			sql="select * from variables.qry">
	</cffunction>
</cfcomponent>