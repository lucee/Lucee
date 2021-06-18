component extends="org.lucee.cfml.test.LuceeTestCase"{
	function beforeAll(){
		
	}
	function run( testResults , testBox ) {
		describe( title="Test suite for LDEV-1980",skip=isNotSupported(), body=function() {
			it(title = "checking cfdbinfo without DB name", body = function( currentSpec ) {
				var ds = getCredentials();
				tableCreation(ds);
				cfdbinfo(datasource=ds,name="local.return_variable",type= "columns",table="TestDsnTBL");
				expect(IsQuery(return_variable)).toBe('true');
				//systemOutput(return_variable,1,1);
			});

			it(title = "checking cfdbinfo with DB name",skip=isNotSupported(), body = function( currentSpec ) {
				var ds = getCredentials();
				tableCreation(ds);
				cfdbinfo(datasource=ds,name="local.pre",type= "columns",table="TestDsnTBL");
				
				cfdbinfo(datasource=ds,name="local.return_variable",type= "columns",table="TestDsnTBL",dbname=pre.TABLE_CAT);
				expect(IsQuery(return_variable)).toBe('true');
			});
		});
	}

	private function tableCreation(ds){
		query name="test" datasource=ds {
			echo("DROP TABLE IF EXISTS `TestDsnTBL`");
		}
		query name="test" datasource=ds {
			echo( "CREATE TABLE `TestDsnTBL`(id varchar(10),Personname varchar(10))");
		}
	}

	function afterAll(){
		if (isNotSupported()) return;
		query name="test" datasource=getCredentials() {
			echo( "DROP TABLE IF EXISTS `TestDsnTBL` ");
		}
	}

	boolean function isNotSupported() {
		var mySql = getCredentials();
		if (structCount(mySql)) {
			return false;
		} else{
			return true;
		}
	}

	private struct function getCredentials() {
		// getting the credentials from the enviroment variables
		return server.getDatasource("mysql");
	}
}