component extends="org.lucee.cfml.test.LuceeTestCase" labels="mysql" {

	function beforeAll() {
		if ( isMySqlNotSupported() ) return;
		variables.creds = mySqlCredentials( true );
	}

	function run( testResults, testBox ) {

		describe( "LDEV-6051 - maxIdle should default to connectionLimit", function() {

			it( title="maxIdle should default to connectionLimit when not set", skip=isMySqlNotSupported(), body=function( currentSpec ) {
				var dsConfig = {
					class: "com.mysql.cj.jdbc.Driver",
					bundleName: "com.mysql.cj",
					connectionString: "jdbc:mysql://#creds.server#:#creds.port#/#creds.database#?useSSL=false&allowPublicKeyRetrieval=true",
					username: creds.username,
					password: creds.password,
					connectionLimit: 5
					// maxIdle NOT set - should default to connectionLimit (5), not Commons Pool2 default (8)
				};

				application action="update" datasources={ "LDEV6051_default": dsConfig };

				// Create pool by running a query
				query datasource="LDEV6051_default" name="local.q" { echo( "SELECT 1" ); }

				// Get pool config
				var poolConfig = getPoolConfig( "LDEV6051_default" );

				//systemOutput( "Test 1: maxIdle=#poolConfig.maxIdle#, maxTotal=#poolConfig.maxTotal#, connectionLimit=5", true );

				// maxIdle should equal connectionLimit (5), not Commons Pool2 default (8)
				expect( poolConfig.maxIdle ).toBe( 5,
					"maxIdle should default to connectionLimit (5) when not explicitly set" );
			});

			it( title="explicit maxIdle should be respected", skip=isMySqlNotSupported(), body=function( currentSpec ) {
				var dsConfig = {
					class: "com.mysql.cj.jdbc.Driver",
					bundleName: "com.mysql.cj",
					connectionString: "jdbc:mysql://#creds.server#:#creds.port#/#creds.database#?useSSL=false&allowPublicKeyRetrieval=true",
					username: creds.username,
					password: creds.password,
					connectionLimit: 10,
					maxIdle: 3 // Explicit maxIdle should be used
				};

				application action="update" datasources={ "LDEV6051_explicit": dsConfig };

				query datasource="LDEV6051_explicit" name="local.q" { echo( "SELECT 1" ); }

				var poolConfig = getPoolConfig( "LDEV6051_explicit" );

				//systemOutput( "Test 2: maxIdle=#poolConfig.maxIdle#, maxTotal=#poolConfig.maxTotal#, connectionLimit=10, explicit maxIdle=3", true );

				expect( poolConfig.maxIdle ).toBe( 3,
					"Explicit maxIdle (3) should be respected" );
			});

			it( title="minIdle should be configurable", skip=isMySqlNotSupported(), body=function( currentSpec ) {
				var dsConfig = {
					class: "com.mysql.cj.jdbc.Driver",
					bundleName: "com.mysql.cj",
					connectionString: "jdbc:mysql://#creds.server#:#creds.port#/#creds.database#?useSSL=false&allowPublicKeyRetrieval=true",
					username: creds.username,
					password: creds.password,
					connectionLimit: 10,
					minIdle: 2
				};

				application action="update" datasources={ "LDEV6051_minIdle": dsConfig };

				query datasource="LDEV6051_minIdle" name="local.q" { echo( "SELECT 1" ); }

				var poolConfig = getPoolConfig( "LDEV6051_minIdle" );

				//systemOutput( "Test 3: minIdle=#poolConfig.minIdle#, maxIdle=#poolConfig.maxIdle#, maxTotal=#poolConfig.maxTotal#", true );

				expect( poolConfig.minIdle ).toBe( 2,
					"minIdle should be configurable" );
			});

		});

	}

	private struct function getPoolConfig( required string dsName ) {
		var result = { maxIdle: -1, minIdle: -1, maxTotal: -1 };
		var pools = getPageContext().getConfig().getDatasourceConnectionPools();
		var iter = pools.iterator();

		while ( iter.hasNext() ) {
			var p = iter.next();
			if ( p.getFactory().getDatasource().getName() == arguments.dsName ) {
				result.maxIdle = p.getMaxIdle();
				result.minIdle = p.getMinIdle();
				result.maxTotal = p.getMaxTotal();
				break;
			}
		}
		return result;
	}

	function isMySqlNotSupported() {
		return isEmpty( mySqlCredentials() );
	}

	private struct function mySqlCredentials( onlyConfig=false ) {
		return server.getDatasource( service="mysql", onlyConfig=arguments.onlyConfig );
	}

}
