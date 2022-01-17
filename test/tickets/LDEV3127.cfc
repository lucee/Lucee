component extends = "org.lucee.cfml.test.LuceeTestCase" {
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
		var enabled = server._getSystemPropOrEnvVars( "lucee.datasource.mssql.modern", "", false);
		systemOutput(enabled, true);
		if (! ( structCount( enabled ) eq 1 && enabled["lucee.datasource.mssql.modern"] eq "true") ){
			//systemOutput("lucee.datasource.mssql.modern not enabled", true);
			return false;
		}

		return structCount(getCredentials());
	}

	private struct function getCredentials() {		
		return server._getSystemPropOrEnvVars( "SERVER, USERNAME, PASSWORD, PORT, DATABASE", "MSSQL_");
	}

	private struct function getDatasource(required string type){
		if( !hasCredentials() ){
			throw "No SQL credentials!";
		}

		var credentials = getCredentials();

		if( arguments.type == "mssql" ){
			return server.getDatasource("mssql");
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