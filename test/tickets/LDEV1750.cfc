<cfcomponent extends="org.lucee.cfml.test.LuceeTestCase" labels="qoq">


<cfscript>
	
	public function beforeAll(){
		variables.qry = queryNew(
			 "name,version"
			,"varchar,varchar"
			,[
				["Railo", "4.2"]
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
		try {
			local.result = _InternalRequest(
				template:createURI("LDEV1750/test.cfm"),
				forms:{}
			);
			assertEquals("template",result.filecontent.trim());
		}
		catch(ee) {
			assertEquals("template",ee.type);
		}
	}

	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}

</cfscript>
	
	<cffunction name="testSqlAttrNobodyTag">
		<cfquery 
			name="local.q2" 
			dbtype="query" 
			sql="select * from variables.qry">
	</cffunction>
</cfcomponent>