component extends="org.lucee.cfml.test.LuceeTestCase" labels="config,datasource" {

	variables.dsName = "ldev6751";
	variables.psqKeys = [ "preserveSingleQuote", "datasourcePreserveSingleQuotes", "psq" ];

	function beforeAll() {
		if ( isNull( request.serverAdminPassword ) ) request.serverAdminPassword = "webweb";
		variables.cs = createObject( "java", "lucee.runtime.config.ConfigServerImpl" );
		variables.config = createObject( "java", "lucee.runtime.config.ConfigUtil" ).getConfigServerImpl( getPageContext().getConfig() );
		variables.dsDef = server.getDatasource( "h2", server._getTempDir( "ldev6751" ) );
		// remember the root psq settings, so they can be restored
		variables.orgPSQ = {};
		var root = config.raw();
		for ( var k in psqKeys ) {
			if ( structKeyExists( root, k ) ) orgPSQ[ k ] = root[ k ];
		}
	}

	function afterAll() {
		var root = config.raw();
		if ( structKeyExists( root, "dataSources" ) && isStruct( root.dataSources ) ) {
			structDelete( root.dataSources, "psq" );
			structDelete( root.dataSources, "preserveSingleQuote" );
			structDelete( root.dataSources, "ldev6751NotADatasource" );
		}
		restoreRootPSQ();
		config.resetPSQL();
		config.resetDataSources();
		try {
			admin action="removeDatasource" type="server" password="#request.serverAdminPassword#" name="#dsName#";
		}
		catch ( e ) {}
	}

	function run( testResults, testBox ) {
		describe( "LDEV-6751 legacy psq key inside dataSources", function() {

			describe( "patchLegacyDataSourcesPSQ()", function() {

				it( "moves psq [""false""] to the root and removes it from dataSources", function() {
					var root = { dataSources: { psq: "false", t: { class: "org.h2.Driver" } } };
					expect( cs.patchLegacyDataSourcesPSQ( root ) ).toBeTrue();
					expect( root ).toHaveKey( "preserveSingleQuote" );
					expect( root.preserveSingleQuote ).toBeFalse();
					expect( root.dataSources ).notToHaveKey( "psq" );
					expect( root.dataSources ).toHaveKey( "t" );
				});

				it( "moves psq [true] (boolean) to the root", function() {
					var root = { dataSources: { psq: true } };
					expect( cs.patchLegacyDataSourcesPSQ( root ) ).toBeTrue();
					expect( root.preserveSingleQuote ).toBeTrue();
					expect( root.dataSources ).notToHaveKey( "psq" );
				});

				it( "moves psq [false] (boolean) to the root", function() {
					var root = { dataSources: { psq: false } };
					expect( cs.patchLegacyDataSourcesPSQ( root ) ).toBeTrue();
					expect( root.preserveSingleQuote ).toBeFalse();
				});

				it( "does not override an existing root setting", function() {
					var root = { preserveSingleQuote: false, dataSources: { psq: true } };
					cs.patchLegacyDataSourcesPSQ( root );
					expect( root.preserveSingleQuote ).toBeFalse();
					expect( root.dataSources ).notToHaveKey( "psq" );

					root = { psq: true, dataSources: { psq: false } };
					cs.patchLegacyDataSourcesPSQ( root );
					expect( root.psq ).toBeTrue();
					expect( root ).notToHaveKey( "preserveSingleQuote" );

					root = { datasourcePreserveSingleQuotes: true, dataSources: { psq: false } };
					cs.patchLegacyDataSourcesPSQ( root );
					expect( root.datasourcePreserveSingleQuotes ).toBeTrue();
					expect( root ).notToHaveKey( "preserveSingleQuote" );
				});

				it( "removes preserveSingleQuote from dataSources without changing the root", function() {
					var root = { dataSources: { preserveSingleQuote: true } };
					expect( cs.patchLegacyDataSourcesPSQ( root ) ).toBeTrue();
					expect( root ).notToHaveKey( "preserveSingleQuote" );
					expect( root.dataSources ).notToHaveKey( "preserveSingleQuote" );
				});

				it( "leaves configs without legacy keys untouched", function() {
					var root = { dataSources: { t: { class: "org.h2.Driver" } } };
					expect( cs.patchLegacyDataSourcesPSQ( root ) ).toBeFalse();
					expect( root ).notToHaveKey( "preserveSingleQuote" );
					expect( cs.patchLegacyDataSourcesPSQ( {} ) ).toBeFalse();
				});
			});

			describe( "live config", function() {

				it( "datasources still load with psq [""false""] inside dataSources", function() {
					var root = config.raw();
					if ( !structKeyExists( root, "dataSources" ) || !isStruct( root.dataSources ) ) root.dataSources = {};
					root.dataSources[ dsName ] = duplicate( dsDef );
					root.dataSources.psq = "false";
					config.resetDataSources();

					var q = queryExecute( "select 1 as x", {}, { datasource: dsName } );
					expect( q.x ).toBe( 1 );
					expect( root.dataSources ).notToHaveKey( "psq" );
				});

				it( "datasources still load with a non struct entry inside dataSources", function() {
					var root = config.raw();
					root.dataSources[ dsName ] = duplicate( dsDef );
					root.dataSources.ldev6751NotADatasource = "false";
					try {
						config.resetDataSources();
						var q = queryExecute( "select 1 as x", {}, { datasource: dsName } );
						expect( q.x ).toBe( 1 );
					}
					finally {
						structDelete( root.dataSources, "ldev6751NotADatasource" );
						config.resetDataSources();
					}
				});

				it( "psq inside dataSources is honoured when the root has no setting", function() {
					var root = config.raw();
					try {
						for ( var k in psqKeys ) structDelete( root, k );
						root.dataSources.psq = true;
						config.resetPSQL();
						config.resetDataSources();
						expect( config.getPSQL() ).toBeTrue();
						expect( root.dataSources ).notToHaveKey( "psq" );

						for ( var k in psqKeys ) structDelete( root, k );
						root.dataSources.psq = "false";
						config.resetPSQL();
						config.resetDataSources();
						expect( config.getPSQL() ).toBeFalse();
						expect( root.dataSources ).notToHaveKey( "psq" );
					}
					finally {
						restoreRootPSQ();
						config.resetPSQL();
					}
				});

				it( "datasources still load after configImport with psq inside dataSources", function() {
					var ds = duplicate( dsDef );
					var data = { dataSources: { psq: "false" } };
					data.dataSources[ dsName ] = ds;
					configImport( data = data, type = "server", password = request.serverAdminPassword );

					var q = queryExecute( "select 1 as x", {}, { datasource: dsName } );
					expect( q.x ).toBe( 1 );
				});
			});
		});
	}

	private function restoreRootPSQ() {
		var root = config.raw();
		for ( var k in psqKeys ) structDelete( root, k );
		for ( var k in orgPSQ ) root[ k ] = orgPSQ[ k ];
	}
}
