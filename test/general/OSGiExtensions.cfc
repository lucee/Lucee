component extends="org.lucee.cfml.test.LuceeTestCase" labels="osgi" {

	function beforeAll() {
		variables.service = getPageContext().getConfig().getFactory().getEngine().getBundleService();
		variables.extensions = getOSGiExtensions();
	}

	function run( testResults, testBox ) {
		describe( "extensions that ship OSGi bundles", function() {

			it( title="at least one such extension is installed", body=function() {
				// the test environment installs several (JDBC drivers, search, image, ...), if this fails nothing below is tested
				expect( arrayLen( extensions ) ).toBeGT( 0, "no installed extension ships OSGi bundles" );
			});

			it( "all their bundles can be loaded", function() {
				var failed = [];
				for ( var ext in extensions ) {
					for ( var b in ext.bundles ) {
						var info = service.loadFromLocal( b.name, b.version, true );
						if ( isNull( info ) ) arrayAppend( failed, ext.name & ": " & b.name & ":" & b.version );
					}
				}
				expect( failed ).toBeEmpty( "bundles that cannot be loaded: " & serializeJSON( failed ) );
			});

			it( "the bundles of extensions that start their bundles are active", function() {
				var notActive = [];
				for ( var ext in extensions ) {
					if ( !ext.startBundles ) continue;
					for ( var b in ext.bundles ) {
						var info = service.getBundleInfo( b.name, b.version );
						if ( isNull( info ) ) continue; // a fragment or an optional bundle that was not loaded
						if ( !info.fragment && info.state != "active" ) arrayAppend( notActive, ext.name & ": " & b.name & ":" & b.version & " is " & info.state );
					}
				}
				expect( notActive ).toBeEmpty( "bundles that are not active: " & serializeJSON( notActive ) );
			});

			it( title="a datasource works with the JDBC driver from an extension bundle", skip=notHasH2(), body=function() {
				var dsName = "OSGiExtensions_ds";
				application action="update" datasources={ "#dsName#": server.getDatasource( "h2", server._getTempDir( "OSGiExtensions" ) ) };
				try {
					expect( queryExecute( "SELECT 1 AS one", {}, { datasource: dsName } ).one ).toBe( 1 );
				}
				finally {
					DBPoolClear( dsName );
					application action="update" datasources={};
				}
			});

		});
	}

	private array function getOSGiExtensions() {
		var result = [];
		var list = extensionList();
		loop query=list {
			var bundles = [];
			if ( isQuery( list.bundles ) ) {
				loop query=list.bundles {
					arrayAppend( bundles, { name: list.bundles.name, version: list.bundles.version } );
				}
			}
			if ( arrayLen( bundles ) ) {
				arrayAppend( result, { name: list.name, bundles: bundles, startBundles: isBoolean( list.startBundles ) && list.startBundles } );
			}
		}
		return result;
	}

	private boolean function notHasH2() {
		return structIsEmpty( server.getDatasource( "h2", server._getTempDir( "OSGiExtensions" ) ) );
	}

}
