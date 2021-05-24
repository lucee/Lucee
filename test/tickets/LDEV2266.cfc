component extends="org.lucee.cfml.test.LuceeTestCase"	{
	
	
	//public function afterTests(){}
	
	public function setUp(){
		variables.has=defineDatasources();
	}


	public void function testH2(){
		
		query datasource="h2" name="local.qry1" cachedWithin=createTimespan(0, 0, 1, 0) {
			echo("select DATEDIFF('MILLISECOND', DATE '1970-01-01', CURRENT_TIMESTAMP()) as a"); // gives seconds since 1970
		}

		sleep(10);

		query datasource="h2" name="local.qry2" cachedWithin=createTimespan(0, 0, 1, 0) {
			echo("select DATEDIFF('MILLISECOND', DATE '1970-01-01', CURRENT_TIMESTAMP()) as a"); // gives seconds since 1970
		}
		assertEquals(qry1.a,qry2.a);
		
	}

	public void function testMySQL(){
		if(!variables.has) return;
		
		query datasource="mysql" name="local.qry1" cachedWithin=createTimespan(0, 0, 1, 0) {
			echo("select UNIX_TIMESTAMP() as a"); // gives seconds since 1970
		}

		sleep(1500);

		query datasource="mysql" name="local.qry2" cachedWithin=createTimespan(0, 0, 1, 0) {
			echo("select UNIX_TIMESTAMP() as a"); // gives seconds since 1970
		}
		assertEquals(qry1.a,qry2.a);
		
	}



	private function defineDatasources() {
		var ds={};

		// H2
		var ds['h2'] = server.getDatasource("h2", "#getDirectoryFromPath(getCurrentTemplatePath())#/datasource/dbh2" );
		
		// MySQL
		var mySQL=getCredentials();
		var has=false;
		if(mySQL.count()>0) {
			var ds['mysql']=mySQL;
			has=true;
		}

		application action="update" datasources=ds;

		return has;
	}

	private struct function getCredentials() {
		return server.getDatasource("mysql");
	}
} 