<cfcomponent extends="org.lucee.cfml.test.LuceeTestCase">
<cfscript>

	//public function afterTests(){}
	
	public function setUp(){
		variables.has=defineDatasource();
	}

	private string function defineDatasource(){
		var mySQL=getCredencials();
		if(mySQL.count()==0) return false;
		application action="update" datasource="#mysql#";
		return true;
	}

	private struct function getCredencials() {
		return server.getDatasource("mysql");
	}

	public void function testCachedWithin(){
		if(!variables.has) return;
		
		query {
			echo("DROP PROCEDURE IF EXISTS `proc_INOUT`");
		}
		query {
			echo("
CREATE PROCEDURE `proc_INOUT` (INOUT var1 INT)
BEGIN
    SET var1 = var1 * 2;
END
			");
		}

		storedproc procedure="proc_INOUT" cachedwithin="#CreateTimeSpan(0,0,10,0)#" {
			procparam  type="inout" variable="local.res" cfsqltype="cf_sql_varchar" value="10";
		}
		storedproc procedure="proc_INOUT" cachedwithin="#CreateTimeSpan(0,0,10,0)#" {
			procparam  type="inout" variable="local.res" cfsqltype="cf_sql_varchar" value="10";
		}
		assertEquals(20,res);
		
	}


</cfscript>
</cfcomponent>