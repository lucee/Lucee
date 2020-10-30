component extends = "org.lucee.cfml.test.LuceeTestCase" {
	function beforeAll(){
		variables.uri = createURI( "LDEV3102" );

		if( hasCredentials() ){
			variables.mssqlDatasource = getDatasource("mssql");
	
			query datasource=variables.mssqlDatasource {
				echo("
					drop table if exists LDEV3102;
					create table LDEV3102 (id int primary key, test varchar(20));

					drop table if exists LDEV3102_NOPKEY;
					create table LDEV3102_NOPKEY ([key] varchar(20), test varchar(20));

					drop table if exists LDEV3102_AUTOPKEY;
					create table LDEV3102_AUTOPKEY (id int identity primary key, test varchar(20));
				");
			}
		}
	}

	public function afterAll(){
		if( hasCredentials() ){
			query datasource=variables.mssqlDatasource {
				echo("
					drop table if exists LDEV3102
					drop table if exists LDEV3102_NOPKEY
					drop table if exists LDEV3102_AUTOPKEY
				");
			}
		}
	}

	function run( testResults, textbox ){
		if( !hasCredentials() ){
			throw "No SQL Server credentials defined!";
		}

		var drivers = [
			  {label="MSSQL", datasource=getDatasource("mssql")}
			, {label="JDDS", datasource=getDatasource("jtds")}
		];

		for( var driver in drivers ){
			describe("testcase for LDEV-3102 using #driver.label# driver", function(){
				beforeEach( function( currentSpec ){
					query datasource=driver.datasource {
						// reset the table state
						echo("
							delete from LDEV3102
							insert into LDEV3102 values (1, 'testcase');

							delete from LDEV3102_NOPKEY
							insert into LDEV3102_NOPKEY values ('fb1b5fc5e', 'testcase');
						");
					}
				});			
	
				it(title = "Select operation in cfquery with name only", body = function ( currentSpec ){
					var data = "";
	
					try {
						query name="data" datasource=driver.datasource {
							echo("select * from LDEV3102");
						}
	
						expect(data.columnData("id")).toBe([1]);
					} catch (Any e){
						throw e.message;
					}
				});
	
				it(title = "Insert and Select operation in cfquery with name only", body = function ( currentSpec ){
					var data = "";
	
					try {
						query name="data" datasource=driver.datasource {
							echo("
								insert into LDEV3102 values (2,'inserted')
								select * from LDEV3102 order by id
							");
						}
	
						expect(data.columnData("id")).toBe([1, 2]);
					} catch (Any e){
						throw e.message;
					}
				});
	
				it(title = "Select operation in cfquery with name and result attribute", body = function ( currentSpec ){
					var data = "";
					var result = "";
	
					try {
						query name="data" result="result" datasource=driver.datasource {
							echo("select * from LDEV3102");
						}
	
						expect(data.columnData("id")).toBe([1]);
					} catch (Any e){
						throw e.message;
					}
				});
	
				it(title = "Insert and Select operation in cfquery with name and result attribute", body = function ( currentSpec ){
					var data = "";
					var result = "";
	
					try {
						query name="data" result="result" datasource=driver.datasource {
							echo("
								insert into LDEV3102 values (2, 'inserted')
								select * from LDEV3102 order by id
							");
						}
	
						expect(data.columnData("id")).toBe([1, 2]);
					} catch (Any e){
						throw e.message;
					}
				});
	
				it(title = "Select and Insert operation in cfquery with name and result attribute", body = function ( currentSpec ){
					var data = "";
					var result = "";
	
					query name="data" result="result" datasource=driver.datasource {
						echo("
							select * from LDEV3102
							insert into LDEV3102 values (2, 'inserted')
						");
					}
	
					expect(data.columnData("id")).toBe([1]);
				});
	
				it(title = "Table variable with insert should return values with name only", body = function ( currentSpec ){
					var data = "";
	
					query name="data" datasource=driver.datasource {
						echo("
							declare @test table(id int primary key)
							insert into @test (id) values (2), (3), (1)
							select id from @test order by id asc
						");
					}
	
					expect(data.columnData("id")).toBe([1, 2, 3]);
				});
	
				it(title = "Table variable with insert should return values with name and result attribute", body = function ( currentSpec ){
					var data = "";
					var result = "";
	
					query name="data" result="result" datasource=driver.datasource {
						echo("
							declare @test table(id int primary key)
							insert into @test (id) values (2), (3), (1)
							select id from @test order by id asc
						");
					}
	
					expect(data.columnData("id")).toBe([1, 2, 3]);
				});
	
				it(title = "Table variable with insert should return values but not generatedKey", body = function ( currentSpec ){
					var data = "";
					var result = "";
	
					query name="data" result="result" datasource=driver.datasource {
						echo("
							declare @test table(id int primary key)
							insert into @test (id) values (2), (3), (1)
							select id from @test order by id asc
						");
					}
	
					expect(structKeyExists(result, "generatedKey")).toBeFalse();
				});
	
				it(title = "Conditional insert/select should return new record as being inserted with name", body = function ( currentSpec ){
					var data = "";
	
					query name="data" datasource=driver.datasource {
						echo("
							if( not exists( select 1 from LDEV3102_NOPKEY where [key] = 'ba8668b9b') ) 
								begin 
									insert into LDEV3102_NOPKEY ( [key] ) values ( 'ba8668b9b' ) 
									select 1 as inserted 
								end 
							else 
								begin 
									select 0 as inserted 
								end
						");
					}
	
					expect(data.columnData("inserted")).toBe([1]);
				});
	
				it(title = "Conditional insert/select should return new record as being inserted name and result", body = function ( currentSpec ){
					var data = "";
					var result = "";
	
					query name="data" result="result" datasource=driver.datasource {
						echo("
							if( not exists( select 1 from LDEV3102_NOPKEY where [key] = 'ba8668b9b') ) 
								begin 
									insert into LDEV3102_NOPKEY ( [key] ) values ( 'ba8668b9b' ) 
									select 1 as inserted 
								end 
							else 
								begin 
									select 0 as inserted 
								end
						");
					}
	
					expect(data.columnData("inserted")).toBe([1]);
				});
	
				it(title = "Conditional insert/select should return new record as *not* being inserted when record exists with name", body = function ( currentSpec ){
					var data = "";
	
					query name="data" datasource=driver.datasource {
						echo("
							if( not exists( select 1 from LDEV3102_NOPKEY where [key] = 'fb1b5fc5e') ) 
								begin 
									insert into LDEV3102_NOPKEY ( [key] ) values ( 'fb1b5fc5e' ) 
									select 1 as inserted 
								end 
							else 
								begin 
									select 0 as inserted 
								end
						");
					}
	
					expect(data.columnData("inserted")).toBe([0]);
				});
	
				it(title = "Conditional insert/select should return new record as *not* being inserted when record exists name and result", body = function ( currentSpec ){
					var data = "";
					var result = "";
	
					query name="data" result="result" datasource=driver.datasource {
						echo("
							if( not exists( select 1 from LDEV3102_NOPKEY where [key] = 'fb1b5fc5e') ) 
								begin 
									insert into LDEV3102_NOPKEY ( [key] ) values ( 'fb1b5fc5e' ) 
									select 1 as inserted 
								end 
							else 
								begin 
									select 0 as inserted 
								end
						");
					}
	
					expect(data.columnData("inserted")).toBe([0]);
				});
	
				it(title = "Conditional insert/select should not return generateKey", body = function ( currentSpec ){
					var data = "";
					var result = "";
	
					query name="data" result="result" datasource=driver.datasource {
						echo("
							if( not exists( select 1 from LDEV3102_NOPKEY where [key] = 'ba8668b9b') ) 
								begin 
									insert into LDEV3102_NOPKEY ( [key] ) values ( 'ba8668b9b' ) 
									select 1 as inserted 
								end 
							else 
								begin 
									select 0 as inserted 
								end
						");
					}
	
					expect(structKeyExists(result, "generatedKey")).toBeFalse();
				});
	
				it(title = "Insert with OUTPUT clause should return generatedKey", body = function ( currentSpec ){
					var data = "";
					var result = "";

					query name="data" result="result" datasource=driver.datasource {
						echo("
							insert into LDEV3102_AUTOPKEY (test) OUTPUT Inserted.id, Inserted.test values ('inserted');
						");
					}

					expect(data.recordCount).toBe(1, "Should return a record!");
					expect(data.id).toBeGT(0, "Unexpected `id` returned from OUTPUT clause!");
					expect(data.test).toBe('inserted', "Unexpected `test` returned from OUTPUT clause!");
					expect(result.generatedKey).toBeGT(0, "Unexpected generatedKey!");
				});
	
				it(title = "Multiple SELECT statements should only return first recordset", body = function ( currentSpec ){
					var data = "";

					query name="data" datasource=driver.datasource {
						echo("
							select 1 as inserted;
							select 2 as updated;
						");
					}

					expect(data.columnData("inserted")).toBe([1]);
				});
	
				it(title = "Multiple SELECT statements should not return generatedKey", body = function ( currentSpec ){
					var data = "";
					var result = "";

					query name="data" result="result" datasource=driver.datasource {
						echo("
							select 1 as inserted;
							select 2 as updated;
						");
					}

					expect(structKeyExists(result, "generatedKey")).toBeFalse();
				});
	
				it(title = "Multiple INSERT INTO should return first generatedKey", body = function ( currentSpec ){
					var data = "";
					var result = "";

					query name="data" result="result" datasource=driver.datasource {
						echo("
							insert into LDEV3102_AUTOPKEY (test) values ('test 1');
							insert into LDEV3102_AUTOPKEY (test) values ('test 2');
						");
					}

					expect(result.generatedKey).toBeGT(0);
				});
	
				it(title = "Creating a temp table and dropping it should still return select statement with name only", body = function ( currentSpec ){
					var data = "";

					query name="data" datasource=driver.datasource {
						echo("
							drop table if exists ##LDEV3102_TEMP_PRIMARY
							drop table if exists ##LDEV3102_TEMP_SECONDARY

							create table ##LDEV3102_TEMP_PRIMARY (id int primary key with (IGNORE_DUP_KEY=ON), unique (id))
							create table ##LDEV3102_TEMP_SECONDARY (id int primary key with (IGNORE_DUP_KEY=ON), unique (id))

							insert into ##LDEV3102_TEMP_PRIMARY (id) values (1), (2), (3), (4), (5), (6);

							insert into ##LDEV3102_TEMP_SECONDARY 
							select id from ##LDEV3102_TEMP_PRIMARY where id % 2 = 0

							select id from ##LDEV3102_TEMP_SECONDARY

							drop table ##LDEV3102_TEMP_PRIMARY
							drop table ##LDEV3102_TEMP_SECONDARY
						");
					}

					expect(data.columnData("id")).toBe([2, 4, 6]);
				});
	
				it(title = "Creating a temp table and dropping it should still return select statement with name and result", body = function ( currentSpec ){
					var data = "";
					var result = "";

					query name="data" result="result" datasource=driver.datasource {
						echo("
							drop table if exists ##LDEV3102_TEMP_PRIMARY
							drop table if exists ##LDEV3102_TEMP_SECONDARY

							create table ##LDEV3102_TEMP_PRIMARY (id int primary key with (IGNORE_DUP_KEY=ON), unique (id))
							create table ##LDEV3102_TEMP_SECONDARY (id int primary key with (IGNORE_DUP_KEY=ON), unique (id))

							insert into ##LDEV3102_TEMP_PRIMARY (id) values (1), (2), (3), (4), (5), (6);

							insert into ##LDEV3102_TEMP_SECONDARY 
							select id from ##LDEV3102_TEMP_PRIMARY where id % 2 = 0

							select id from ##LDEV3102_TEMP_SECONDARY

							drop table ##LDEV3102_TEMP_PRIMARY
							drop table ##LDEV3102_TEMP_SECONDARY
						");
					}

					debug(data);

					expect(data.columnData("id")).toBe([2, 4, 6]);
				});
			});
		}
	}

	private string function createURI(string calledName){
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}


	private boolean function hasCredentials() {
		return structCount(getCredentials());
	}

	private struct function getCredentials() {
		// parses a scope fo the credentials
		var parseCredentials = function (struct scope){
			var results = {};

			if(
				!isNull(arguments.scope.MSSQL_SERVER) && 
				!isNull(arguments.scope.MSSQL_USERNAME) && 
				!isNull(arguments.scope.MSSQL_PASSWORD) && 
				!isNull(arguments.scope.MSSQL_PORT) && 
				!isNull(arguments.scope.MSSQL_DATABASE)
			) {
				results.server=arguments.scope.MSSQL_SERVER;
				results.username=arguments.scope.MSSQL_USERNAME;
				results.password=arguments.scope.MSSQL_PASSWORD;
				results.port=arguments.scope.MSSQL_PORT;
				results.database=arguments.scope.MSSQL_DATABASE;
			}

			return results;
		}

		// check the credentials in the enviroment variables
		var msSQL = parseCredentials(server.system.environment);

		// if not found in the environment varialbes, check in the system variables
		if( !structCount(msSQL) ){
			msSQL = parseCredentials(server.system.properties);
		}

		return msSQL;
	}

	private struct function getDatasource(required string type){
		if( !hasCredentials() ){
			throw "No SQL credentials!";
		}

		var credentials = getCredentials();

		if( arguments.type == "mssql" ){
			return {
					class: 'com.microsoft.sqlserver.jdbc.SQLServerDriver'
				, bundleName: 'com.microsoft.sqlserver.mssql-jdbc'
				, bundleVersion: '7.0.0'
				, connectionString: 'jdbc:sqlserver://' & credentials.server & ':' & credentials.port & ';databaseName=' & credentials.database & ';sendStringParametersAsUnicode=true;selectMethod=direct'
				, username: credentials.username
				, password: credentials.password
				, blob:true
				, clob:true
				, validate:false
			};
		} else if( arguments.type == "jtds" ){
			return {
					class: 'net.sourceforge.jtds.jdbc.Driver'
				, bundleName: 'jtds'
				, bundleVersion: '1.3.1'
				, connectionString: 'jdbc:jtds:sqlserver://' & credentials.server & ':' & credentials.port & '/' & credentials.database
				, username: credentials.username
				, password: credentials.password
				, blob:true
				, clob:true
				, validate:false
			};
		} else {
			throw "Invalid datasource specified!";
		}
	}

}