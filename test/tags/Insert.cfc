component extends="org.lucee.cfml.test.LuceeTestCase"  labels="mysql" {
	function beforeAll(){
		if(isNotSupported()) return;
		request.mySQL = getCredentials();
		request.mySQL.storate = true;
		variables.str = request.mySQL;
		tableCreation();
	}

	function run( testResults , testBox ) {
		describe( title="Test suite for cfinsert",skip=isNotSupported(), body=function() {
			it(title = "checking cfinsert tag", body = function( currentSpec ) {
				form.id =1 
				form.personName ="testCase" 
				cfinsert (tableName = "cfInsertTBL" formFields = "id,personName" datasource=str);
				query datasource=str name="testQry"{
					echo("SELECT * FROM `cfInsertTBL`");
				}
				expect(testQry.personName).toBe('testcase');
			});
		});
	}


	private function tableCreation(){
		query datasource=str{
			echo("DROP TABLE IF EXISTS `cfInsertTBL`");
		}
		query datasource=str{
			echo( "
				create table `cfInsertTBL`(id varchar(10),Personname varchar(10))"
				);
		}
	}

	private boolean function isNotSupported() {
		var cred=getCredentials();
		return isNull(cred) || structCount(cred)==0;
	}

	private struct function getCredentials() {
		return server.getDatasource("mysql");
	}

	Function afterAll(){
		if(isNotSupported()) return;
		query datasource=str{
			echo("DROP TABLE IF EXISTS `cfInsertTBL`");
		}
	}
}