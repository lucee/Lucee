component extends="org.lucee.cfml.test.LuceeTestCase" {
	function beforeAll() {
		if(isNotSupported()) return;
		request.mssql = getCredentials();
		request.mssql.storage = true;
		variables.str = request.mssql;
		tableCreation();
	}

	function run( testResults , testBox ) {
		describe( title = "Test suite for LDEV-4753", body = function() {
			it( title = "checking CFUPDATE for LDEV-4753", body = function( currentSpec ) {
				param name="form.id" default="1";
				param name="form.myValue" default="LuceeTestCase";
				param name="form.seqno" default="";
				expect( function() {
							cfupdate(tableName = "cfupdatetbl" formFields = "form.id,form.myValue,form.seqno" datasource=str);
						}).notToThrow();
			});
		});
	}


	private function tableCreation() {
		query datasource=str{
			echo("DROP TABLE IF EXISTS cfupdatetbl");
		}
		query datasource=str{
			echo("
					create table cfupdatetbl (id numeric(18, 0) primary key,myValue nvarchar(50),seqno numeric(18, 0))"
				);
		}
	}


	private boolean function isNotSupported() {
		var cred=getCredentials();
		return isNull(cred) || structCount(cred)==0;
	}

	private struct function getCredentials() {
		return server.getDatasource("mssql");
	}

	function afterAll() {
		if(isNotSupported()) return;
		query datasource=str{
			echo("DROP TABLE IF EXISTS cfupdatetbl");
		}
	}
}