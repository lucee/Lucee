component extends="org.lucee.cfml.test.LuceeTestCase"{
	function beforeAll(){
		application action="update" 
			datasource={
	  		class: 'org.hsqldb.jdbcDriver'
			, bundleName: "org.hsqldb.hsqldb"
			, connectionString: 'jdbc:hsqldb:file:#getDirectoryFromPath(getCurrentTemplatePath())#/datasource/db'
		};

		try{
			query {
				echo("drop TABLE T2260");
			}
		}
		catch(local.e){}
		
		query  {
			echo("CREATE TABLE T2260 (");
			echo("id int NOT NULL,");
			echo("i int,");		
			echo("vc varchar(255)");		
			echo(") ");
		}

		query  {
			echo("insert into T2260(id, i, vc) values(1,2,'3')");
		}
	}

		function afterAll(){
			try{
				query {
					echo("drop TABLE T2260");
				}
			}
			catch(local.e){}
		}

	function run( testResults , testBox ) {
		describe( "Test case for LDEV2260", function() {
			it( title='test int length', body=function( currentSpec ) {
			 	query name="local.q" {
					echo("select * from T2260 where id=");
					queryparam
						value = 1.23456789
						sqltype = 'integer'
						maxLength = 1;
				}
				//expect(isStruct(local.result)).tobe(true);
			});
		});
	}
		


}