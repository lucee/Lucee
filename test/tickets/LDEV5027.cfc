component extends="org.lucee.cfml.test.LuceeTestCase" labels="cache" {

	function beforeAll() {
		variables.adminCfc = new org.lucee.cfml.Administrator( "server", request.ServerAdminPassword );
		variables.cacheName = "ldev5027_" & lCase( hash( createUUID(), "quick" ) );
	}

	function afterAll() {
		try {
			adminCfc.removeCacheConnection( variables.cacheName );
		}
		catch ( any e ) {}
	}

	function run( testResults, testBox ) {
		describe( "LDEV-5027 Administrator.cfc updateCacheConnection() with an empty default", function() {

			it( title="creates a new cache connection with default=''", body=function( currentSpec ) {
				// threw "Invalid default type [false]" because an empty default fell back to false
				adminCfc.updateCacheConnection(
					class: "lucee.runtime.cache.ram.RamCache",
					name: variables.cacheName,
					bundleName: "",
					bundleVersion: "",
					default: "",
					readonly: false,
					storage: false,
					custom: { "timeToIdleSeconds": "1800", "timeToLiveSeconds": "3600" }
				);
				var conn = adminCfc.getCacheConnection( variables.cacheName );
				expect( conn.name ).toBe( variables.cacheName );
				expect( conn.default ).toBe( "" );
			});

			it( title="updates an existing cache connection without passing default", body=function( currentSpec ) {
				adminCfc.updateCacheConnection(
					class: "lucee.runtime.cache.ram.RamCache",
					name: variables.cacheName,
					custom: { "timeToIdleSeconds": "900", "timeToLiveSeconds": "3600" }
				);
				var conn = adminCfc.getCacheConnection( variables.cacheName );
				expect( conn.default ).toBe( "" );
				expect( conn.custom.timeToIdleSeconds ).toBe( 900 );
			});
		});
	}
}
