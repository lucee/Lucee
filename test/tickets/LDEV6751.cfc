component extends="org.lucee.cfml.test.LuceeTestCase" labels="config,datasource" {

	variables.dsName = "ldev6751";
	variables.otherKey = "ldev6751NotADatasource";
	variables.rootKeys = [ "preserveSingleQuote", "datasourcePreserveSingleQuotes", "psq" ];
	variables.dataSourcesKeys = [ "psq", "preserveSingleQuote" ];

	function beforeAll() {
		if ( isNull( request.serverAdminPassword ) ) request.serverAdminPassword = "webweb";
		variables.csi = createObject( "java", "lucee.runtime.config.ConfigServerImpl" );
		variables.dsDef = server.getDatasource( "h2", server._getTempDir( "ldev6751" ) );
		// remember the psq settings, so they can be restored exactly
		variables.snapshot = takeSnapshot();
	}

	function afterAll() {
		restoreSnapshot();
		var root = getRoot();
		if ( structKeyExists( root, "dataSources" ) && isStruct( root.dataSources ) ) {
			structDelete( root.dataSources, dsName );
			structDelete( root.dataSources, otherKey );
		}
		// persist the restored config, configImport and the admin action stored the modified one
		var config = getConfig();
		createObject( "java", "lucee.runtime.config.ConfigAdmin" )
			.newInstance( config, config.isPasswordEqual( request.serverAdminPassword ) )
			.store();
		getConfig().resetPSQL();
		getConfig().resetDataSources();
	}

	function run( testResults, testBox ) {
		describe( "LDEV-6751 psq inside dataSources", function() {

			describe( "precedence (ConfigServerImpl.readPSQ)", function() {

				it( "defaults to false (escape)", function() {
					expect( readPSQ( {} ) ).toBeFalse();
					expect( readPSQ( { dataSources: {} } ) ).toBeFalse();
				});

				it( "default config of 6.0 - 7.1 (still used by older installations) escapes", function() {
					// dataSources.preserveSingleQuote has the legacy (inverted) meaning, "yes" means escape
					expect( readPSQ( { preserveSingleQuote: false, dataSources: { preserveSingleQuote: "yes" } } ) ).toBeFalse();
				});

				it( "default config of 8.0 still escapes", function() {
					expect( readPSQ( { preserveSingleQuote: false, dataSources: {} } ) ).toBeFalse();
				});

				it( "dataSources.psq overrides the root", function() {
					expect( readPSQ( { preserveSingleQuote: false, dataSources: { psq: true } } ) ).toBeTrue();
					expect( readPSQ( { preserveSingleQuote: true, dataSources: { psq: false } } ) ).toBeFalse();
					expect( readPSQ( { preserveSingleQuote: true, dataSources: { psq: "false" } } ) ).toBeFalse();
					expect( readPSQ( { datasourcePreserveSingleQuotes: false, dataSources: { psq: "true" } } ) ).toBeTrue();
					expect( readPSQ( { dataSources: { psq: true } } ) ).toBeTrue();
				});

				it( "dataSources.psq overrides dataSources.preserveSingleQuote", function() {
					expect( readPSQ( { dataSources: { psq: true, preserveSingleQuote: true } } ) ).toBeTrue();
					expect( readPSQ( { dataSources: { psq: false, preserveSingleQuote: false } } ) ).toBeFalse();
				});

				it( "dataSources.preserveSingleQuote has the inverted (legacy) meaning", function() {
					expect( readPSQ( { dataSources: { preserveSingleQuote: true } } ) ).toBeFalse();
					expect( readPSQ( { dataSources: { preserveSingleQuote: "yes" } } ) ).toBeFalse();
					expect( readPSQ( { dataSources: { preserveSingleQuote: false } } ) ).toBeTrue();
					expect( readPSQ( { dataSources: { preserveSingleQuote: "no" } } ) ).toBeTrue();
				});

				it( "dataSources.preserveSingleQuote overrides the root", function() {
					expect( readPSQ( { preserveSingleQuote: false, dataSources: { preserveSingleQuote: false } } ) ).toBeTrue();
					expect( readPSQ( { preserveSingleQuote: true, dataSources: { preserveSingleQuote: "yes" } } ) ).toBeFalse();
					expect( readPSQ( { datasourcePreserveSingleQuotes: true, dataSources: { preserveSingleQuote: true } } ) ).toBeFalse();
				});

				it( "root preserveSingleQuote overrides root datasourcePreserveSingleQuotes", function() {
					expect( readPSQ( { preserveSingleQuote: false, datasourcePreserveSingleQuotes: true } ) ).toBeFalse();
					expect( readPSQ( { preserveSingleQuote: true, datasourcePreserveSingleQuotes: false } ) ).toBeTrue();
					expect( readPSQ( { preserveSingleQuote: "true" } ) ).toBeTrue();
				});

				it( "root datasourcePreserveSingleQuotes overrides root psq", function() {
					expect( readPSQ( { datasourcePreserveSingleQuotes: true } ) ).toBeTrue();
					expect( readPSQ( { datasourcePreserveSingleQuotes: false } ) ).toBeFalse();
					expect( readPSQ( { datasourcePreserveSingleQuotes: false, psq: true } ) ).toBeFalse();
					expect( readPSQ( { preserveSingleQuote: false, psq: true } ) ).toBeFalse();
				});

				it( "root psq is used when nothing else is set", function() {
					expect( readPSQ( { psq: true } ) ).toBeTrue();
					expect( readPSQ( { psq: false } ) ).toBeFalse();
					expect( readPSQ( { psq: true, dataSources: { psq: false } } ) ).toBeFalse();
					expect( readPSQ( { psq: false, dataSources: { preserveSingleQuote: false } } ) ).toBeTrue();
				});

				it( "invalid values inside dataSources are ignored", function() {
					expect( readPSQ( { preserveSingleQuote: true, dataSources: { psq: "", preserveSingleQuote: "maybe" } } ) ).toBeTrue();
					expect( readPSQ( { preserveSingleQuote: true, dataSources: { psq: "maybe" } } ) ).toBeTrue();
					expect( readPSQ( { preserveSingleQuote: true, dataSources: { psq: {} } } ) ).toBeTrue();
				});

				it( "dataSources that is not a struct is ignored", function() {
					expect( readPSQ( { preserveSingleQuote: true, dataSources: "false" } ) ).toBeTrue();
				});

				it( "does not modify the config data", function() {
					var root = { preserveSingleQuote: false, dataSources: { psq: true, preserveSingleQuote: "yes" } };
					readPSQ( root );
					expect( root ).toBe( { preserveSingleQuote: false, dataSources: { psq: true, preserveSingleQuote: "yes" } } );
				});
			});

			describe( "live config", function() {

				it( "dataSources.psq overrides the root and the keys are kept", function() {
					try {
						var root = getRoot();
						ensureDataSources( root );
						root.preserveSingleQuote = false;
						root.dataSources.psq = true;
						reset();
						expect( getConfig().getPSQL() ).toBeTrue();
						expect( getRoot().dataSources ).toHaveKey( "psq" );
						expect( getRoot() ).toHaveKey( "preserveSingleQuote" );

						root.dataSources.psq = "false";
						root.preserveSingleQuote = true;
						reset();
						expect( getConfig().getPSQL() ).toBeFalse();
						expect( getRoot().dataSources.psq ).toBe( "false" );
					}
					finally {
						restoreSnapshot();
					}
				});

				it( "datasources still load with psq settings and a non struct entry inside dataSources", function() {
					try {
						var root = getRoot();
						ensureDataSources( root );
						root.dataSources[ dsName ] = duplicate( dsDef );
						root.dataSources.psq = "false";
						root.dataSources.preserveSingleQuote = "yes";
						root.dataSources[ otherKey ] = "false";
						reset();

						var q = queryExecute( "select 1 as x", {}, { datasource: dsName } );
						expect( q.x ).toBe( 1 );
						expect( getRoot().dataSources ).toHaveKey( "psq" );
						expect( getRoot().dataSources ).toHaveKey( "preserveSingleQuote" );
						expect( getRoot().dataSources ).toHaveKey( otherKey );
					}
					finally {
						structDelete( getRoot().dataSources, otherKey );
						restoreSnapshot();
					}
				});

				it( "admin updatePSQ is not masked by settings inside dataSources", function() {
					try {
						var root = getRoot();
						ensureDataSources( root );
						root.dataSources.psq = false;
						root.dataSources.preserveSingleQuote = "yes";
						reset();

						admin action="updatePSQ" type="server" password="#request.serverAdminPassword#" psq=true;
						admin action="getDatasourceSetting" type="server" password="#request.serverAdminPassword#" returnVariable="local.settings";
						expect( settings.psq ).toBeTrue();
						expect( getConfig().getPSQL() ).toBeTrue();
						expect( getRoot().preserveSingleQuote ).toBeTrue();
						expect( getRoot().dataSources ).notToHaveKey( "psq" );
						expect( getRoot().dataSources ).notToHaveKey( "preserveSingleQuote" );

						// root aliases cannot mask the admin either
						getRoot().datasourcePreserveSingleQuotes = true;
						getRoot().psq = true;
						admin action="updatePSQ" type="server" password="#request.serverAdminPassword#" psq=false;
						expect( getConfig().getPSQL() ).toBeFalse();
						expect( getRoot() ).notToHaveKey( "datasourcePreserveSingleQuotes" );
						expect( getRoot() ).notToHaveKey( "psq" );

						// reset goes back to the default (escape)
						admin action="updatePSQ" type="server" password="#request.serverAdminPassword#" psq=true;
						getRoot().datasourcePreserveSingleQuotes = true;
						admin action="updatePSQ" type="server" password="#request.serverAdminPassword#" psq="";
						expect( getConfig().getPSQL() ).toBeFalse();
						expect( getRoot() ).notToHaveKey( "preserveSingleQuote" );
						expect( getRoot() ).notToHaveKey( "datasourcePreserveSingleQuotes" );
					}
					finally {
						restoreSnapshot();
					}
				});

				it( "settings inside dataSources are kept after configImport", function() {
					try {
						var data = { preserveSingleQuote: false, dataSources: { psq: true } };
						data.dataSources[ dsName ] = duplicate( dsDef );
						configImport( data = data, type = "server", password = request.serverAdminPassword );

						var root = getRoot();
						expect( root.dataSources ).toHaveKey( "psq" );
						expect( root.dataSources ).toHaveKey( dsName );
						expect( getConfig().getPSQL() ).toBeTrue();
						var stored = deserializeJSON( fileRead( getConfig().getConfigFile() ) );
						expect( stored.dataSources ).toHaveKey( "psq" );

						var q = queryExecute( "select 1 as x", {}, { datasource: dsName } );
						expect( q.x ).toBe( 1 );
					}
					finally {
						restoreSnapshot();
					}
				});
			});
		});
	}

	private boolean function readPSQ( required struct root ) {
		return csi.readPSQ( getConfig(), arguments.root );
	}

	private function getConfig() {
		return createObject( "java", "lucee.runtime.config.ConfigUtil" ).getConfigServerImpl( getPageContext().getConfig() );
	}

	// the raw config data (root struct) of the server config
	private function getRoot() {
		return getConfig().raw();
	}

	private function ensureDataSources( required struct root ) {
		if ( !structKeyExists( arguments.root, "dataSources" ) || !isStruct( arguments.root.dataSources ) ) arguments.root.dataSources = {};
	}

	private function reset() {
		getConfig().resetPSQL();
		getConfig().resetDataSources();
	}

	private struct function takeSnapshot() {
		var root = getRoot();
		var snap = { root: {}, dataSources: {} };
		for ( var k in rootKeys ) {
			if ( structKeyExists( root, k ) ) snap.root[ k ] = root[ k ];
		}
		if ( structKeyExists( root, "dataSources" ) && isStruct( root.dataSources ) ) {
			for ( var k in dataSourcesKeys ) {
				if ( structKeyExists( root.dataSources, k ) ) snap.dataSources[ k ] = root.dataSources[ k ];
			}
		}
		return snap;
	}

	private function restoreSnapshot() {
		var root = getRoot();
		for ( var k in rootKeys ) structDelete( root, k );
		structAppend( root, snapshot.root, true );
		ensureDataSources( root );
		for ( var k in dataSourcesKeys ) structDelete( root.dataSources, k );
		structAppend( root.dataSources, snapshot.dataSources, true );
		reset();
	}
}
