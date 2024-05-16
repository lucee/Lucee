component extends="org.lucee.cfml.test.LuceeTestCase"  labels="mysql" { 
	function beforeAll(){
		if(isNotSupported()) return;
		request.mySQL = getCredentials();
		request.mySQL.storage = true;
		variables.str = request.mySql;
		tableCreation();
	}

	function run( testResults , testBox ) {
		describe( title="Test suite for CFupdate",skip=isNotSupported(), body=function() {
			it(title = "checking CFUPDATE tag", body = function( currentSpec ) {
				form.id =1; 
				form.myValue ="LuceeTestCase";
				cfupdate(tableName = "cfupdatetbl" formFields = "id,myValue" datasource=str);
				query datasource=str name="testQry"{
					echo("SELECT * FROM `cfupdatetbl`");
				}
				expect(testQry.myValue).toBe('LuceeTestCase');
			});
		});
	}


	private function tableCreation(){
		query datasource=str{
			echo("DROP TABLE IF EXISTS `cfupdatetbl`");
		}
		query datasource=str{
			echo( "
				create table `cfupdatetbl`(id varchar(10) NOT NULL PRIMARY KEY,myValue varchar(50))"
				);
		}
		query datasource=str{
			echo( "
				INSERT INTO `cfupdatetbl` values(1,'testCase')"
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
			echo("DROP TABLE IF EXISTS `cfupdatetbl`");
		}
	}
}