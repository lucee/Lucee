component extends="org.lucee.cfml.test.LuceeTestCase" {
	function beforeAll() {
		variables.test = queryNew("id,name,cmpnyName","integer,varchar,varchar", [[01,'saravana', 'MitrahSoft'],[07, 'pothys','MitrahSoft'], [09, 'MichaelOffener','RASIA']]);
		variables.msSQL = server.getDatasource("mssql");
		
		if( structCount(msSQL) ) {
			// define datasource
			application action="update"  datasource=msSQL;

			query{
				echo( "DROP TABLE IF EXISTS LDEV1532" );
			}
			query{
				echo( "CREATE TABLE LDEV1532(id int, name varchar(255))" );
			}
			query{
				echo( "INSERT INTO LDEV1532 VALUES (1, 'test')" );
			}
		}
	}

	function afterAll() {
		if (!notHasMssql()) {
			queryExecute( sql="DROP TABLE IF EXISTS LDEV1532", options: {
				datasource: server.getDatasource("mssql")
			}); 
		}
	}

	function run( testResults , testBox ) {
		describe( "test case for LDEV-1532", function() {
			/*
			it(title = "Checking cfqueryparam with datatype cf_sql_integer, null=false & value is null (dbtype=query)", body = function( currentSpec ) {
				try {
					hasError = false;
					p = [id= { cfsqltype='cf_sql_integer', value='', null='false' } ];
					cfquery( name="qTest" dbtype="query" params=p) {
						echo(" SELECT * FROM test WHERE id = :id ");
					}
				}
				catch(any e) {
					hasError = true
				}
				expect(hasError).toBe('true');
			});	
			
			it(title = "Checking cfqueryparam with datatype cf_sql_integer, null=true & value is null (dbtype=query)", body = function( currentSpec ) {
				p = [id= { cfsqltype='cf_sql_integer', value='', null='true' } ];
				query name="qTest", dbtype="query" params=p {
					echo(" SELECT * FROM test WHERE id = :id ");
				}
				expect(qTest.recordcount).toBe("0");
			});
			
			it(title = "Checking cfqueryparam with datatype cf_sql_integer, null=false & value is not null (dbtype=query)", body = function( currentSpec ) {
				p = [id= { cfsqltype='cf_sql_integer', value='01', null='false' } ];
				cfquery(  name="qTest" dbtype="query"  params=p ) {
					echo(" SELECT * FROM test WHERE id = :id ")
				} 
				expect(qTest.recordcount).toBe("1");
			});
				
			it(title = "Checking cfqueryparam with datatype cf_sql_integer, null=true & value is not null (dbtype=query)", body = function( currentSpec ) {
				p = [id= { cfsqltype='cf_sql_integer', value='01', null='true' } ];
				cfquery( name="qTest" dbtype="query"  params=p ) {
					echo(" SELECT * FROM test WHERE id = :id ")
				}
				expect(qTest.recordcount).toBe('0');
			});
			*/
			// LDEV-4410 don't auto cast numerics to null
			it(title = "Checking query param with datatype cf_sql_integer, null=false & value is null (datasource query)", skip=notHasMssql(), body = function( currentSpec ) {
				expect( function(){
					var	p = [id= { cfsqltype='cf_sql_integer', value='', null='false' } ];
					cfquery( name="local.qTest" params=p ) {
						echo(" SELECT * FROM LDEV1532 WHERE id = :id ");
					}
				}).notToThrow();
			});

			it(title = "Checking cfqueryparam with datatype cf_sql_integer, null=true & value is null (datasource query)", skip=notHasMssql(), body = function( currentSpec ) {
				expect( function(){
					var	p = [id= { cfsqltype='cf_sql_varchar', value='', null='false' } ];
					cfquery( name="local.qTest" params=p ) {
						echo(" SELECT * FROM LDEV1532 WHERE id = :id ");
					}
				}).notToThrow();
			});

			it(title = "Checking cfqueryparam with datatype cf_sql_integer, null=false & value is null (datasource query)", skip=notHasMssql(), body = function( currentSpec ) {
				expect(function(){
				```
					<cfquery name="local.qTest">
						SELECT * FROM LDEV1532 WHERE id = <cfqueryparam sqltype="integer" value="">
					</cfquery> 
					```
				}).notToThrow();
			});

			it(title = "Checking cfqueryparam with datatype cf_sql_varchar, null=false & value is null (datasource query)", skip=notHasMssql(), body = function( currentSpec ) {
				expect(function(){
					```
					<cfquery name="local.qTest">
						SELECT * FROM LDEV1532 WHERE id = <cfqueryparam sqltype="varchar" value="">
					</cfquery> 
					```
				}).notToThrow();
			});
			/*
			it(title = "Checking cfqueryparam with datatype cf_sql_integer, null=true & value is null (datasource query)", body = function( currentSpec ) {
				p = [id= { cfsqltype='cf_sql_integer', value='', null='true' } ];
				cfquery( name="qTest" params=p) {
					echo(" SELECT * FROM LDEV1532 WHERE id = :id ")
				}
				expect(qTest.recordcount).toBe("0");
			});
			
			it(title = "Checking cfqueryparam with datatype cf_sql_integer, null=false & value is not null (datasource query)", skip=notHasMssql(), body = function( currentSpec ) {
				p = [id= { cfsqltype='cf_sql_integer', value='1', null='false' } ];
				cfquery( name="qTest" params=p) {
					echo(" SELECT * FROM LDEV1532 WHERE id = :id ")
				}
				expect(qTest.recordcount).toBe("1");
			});
			
			it(title = "Checking cfqueryparam with datatype cf_sql_integer, null=true & value is not null (datasource query)", skip=notHasMssql(), body = function( currentSpec ) {
				p = [id= { cfsqltype='cf_sql_integer', value='1', null='true' } ];
				cfquery( name="qTest" params=p) {
					echo(" SELECT * FROM LDEV1532 WHERE id = :id ")
				}
				expect(qTest.recordcount).toBe('0');
			});
			*/
		});
	}

	private boolean function notHasMssql() {
		return !structCount(server.getDatasource("mssql"));
	}

}
