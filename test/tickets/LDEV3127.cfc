component extends = "org.lucee.cfml.test.LuceeTestCase" labels="mssql"{
	function beforeAll(){
	}

	public function afterAll(){
	}

	function run( testResults, textbox ){
		if( !hasCredentials() ){
			// TODO handle better
			return;
		}

		var drivers = [
			  {label="MSSQL", datasource=getDatasource("mssql")}
			, {label="JDDS", datasource=getDatasource("jtds")}
		];

		for( var data in drivers ){
			describe("testcase for LDEV-XXXX using #data.label# driver", function(){
				it(title = "Should throw custom RAISERROR in #data.label#", data=data, body = function ( currentSpec ){
					var exceptionMessage = "[no exception found]";
					
					try {
						query datasource=arguments.data.datasource {
							echo("
								select 1

								raiserror('Oops! Something went wrong!', 16, 1);
							");
						}
					} catch (Any e){
						exceptionMessage = e.message;
					}
		
					expect(exceptionMessage).toBe("Oops! Something went wrong!", "Unexpected exception message!");
				});

				it(title = "Should throw custom RAISERROR when multiple statements in #data.label#", data=data, body = function ( currentSpec ){
					var exceptionMessage = "[no exception found]";
					
					try {
						query datasource=arguments.data.datasource {
							echo("
								declare @test table(id int primary key)
								insert into @test (id) values (2), (3), (1)
	
								raiserror('Oops! Something went wrong!', 16, 1);
	
								select id from @test order by id asc
							");
						}
					} catch (Any e){
						exceptionMessage = e.message;
					}
		
					expect(exceptionMessage).toBe("Oops! Something went wrong!", "Unexpected exception message!");
				});

			});
		}
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
				, bundleName: 'org.lucee.mssql'
				, bundleVersion: '7.2.2.jre8'
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