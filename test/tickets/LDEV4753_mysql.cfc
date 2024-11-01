component extends="org.lucee.cfml.test.LuceeTestCase" labels="mysql" {
	function beforeAll() {
		if(isNotSupported()) return;
		var mysql = getCredentials();
		mysql.storage = true;
		variables.datasource = mysql;
		tableCreation();
	}

	function afterAll() {
		if(isNotSupported()) return;
		query datasource=variables.datasource{
			echo("DROP TABLE IF EXISTS LDEV4753");
		}
	}

	function run( testResults , testBox ) {
		describe( title = "Test suite for LDEV-4753 with MSSQL", body = function() {
			it( title = "checking CFINSERT for LDEV-4753 with empty numeric cols", body = function( currentSpec ) {
				param name="form.id" default="1";
				param name="form.myValue" default="LuceeTestCase";
				param name="form.seqno" default="";
				cfinsert(tableName = "LDEV4753" formFields = "form.id,form.myValue,form.seqno" datasource=variables.datasource);
				checkTable( 1 );
			});
			it( title = "checking CFUPDATE for LDEV-4753 with empty numeric cols", body = function( currentSpec ) {
				param name="form.id" default="1";
				param name="form.myValue" default="LDEV-4753";
				param name="form.seqno" default="";
				cfupdate(tableName = "LDEV4753" formFields = "form.id,form.myValue,form.seqno" datasource=variables.datasource);
				checkTable( 1 );
				form.seqno="3";
				form.myValue="";
				cfupdate(tableName = "LDEV4753" formFields = "form.id,form.myValue,form.seqno" datasource=variables.datasource);
				checkTable( 1 );
			});
		});
	}


	private function tableCreation() {
		query datasource=variables.datasource{
			echo("DROP TABLE IF EXISTS LDEV4753");
		}
		query datasource=variables.datasource{
			echo("create table LDEV4753 (id numeric(18, 0) primary key,myValue nvarchar(50),seqno numeric(18, 0))");
		}
	}

	private function checkTable( id ){
		var params = {
			id = {value: arguments.id, sql_type: "numeric" }
		}
		query name="local.q" datasource=variables.datasource params=params {
			echo("select * from LDEV4753 where id = :id");
		}
		systemOutput( q, true );
		loop list="id,myvalue,seqno" item="local.c"{
			expect( q[ c ] ).toBe( form[ c ] );
		}
	}

	private boolean function isNotSupported() {
		var cred=getCredentials();
		return isNull(cred) || structCount(cred)==0;
	}

	private struct function getCredentials() {
		return server.getDatasource("mysql");
	}

	
}
