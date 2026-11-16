component extends="org.lucee.cfml.test.LuceeTestCase" labels="cache" {

	function beforeAll() {
		variables.cacheName = "ldev6921";
		var settings = getApplicationSettings();
		variables.origCaches = structKeyExists( settings, "caches" ) && isStruct( settings.caches ) ? duplicate( settings.caches ) : {};
		var caches = duplicate( variables.origCaches );
		caches[ variables.cacheName ] = {
			class: "lucee.runtime.cache.ram.RamCache",
			storage: false,
			custom: { timeToIdleSeconds: "0", timeToLiveSeconds: "0" },
			default: ""
		};
		application action="update" caches="#caches#";
	}

	function afterAll() {
		application action="update" caches="#variables.origCaches#";
	}

	function run( testResults, testBox ) {
		describe( "LDEV-6921 cfcache with RAMCache", function() {

			beforeEach( function() {
				cacheClear( cacheName=variables.cacheName );
			});

			it( "cachePut and cacheGet still work with the RAM cache", function() {
				cachePut( id="ldev6921-put", value="ok", cacheName=variables.cacheName );
				expect( cacheGet( id="ldev6921-put", cacheName=variables.cacheName ) ).toBe( "ok" );
			});

			it( "cfcache action=content does not throw and caches the body", function() {
				assertCached( "content", "ldev6921-content" );
			});

			it( "cfcache action=cache with a body does not throw and caches the body", function() {
				assertCached( "cache", "ldev6921-cache" );
			});

		});
	}

	private function assertCached( required string action, required string key ) {
		var token = createUUID();
		var first = "";
		savecontent variable="first" {
			cfcache( action=arguments.action, key=arguments.key, cachename=variables.cacheName, timespan=createTimespan( 0, 1, 0, 0 ) ) {
				echo( "Hello " & token );
			}
		}
		expect( first ).toInclude( "Hello " & token );

		var token2 = createUUID();
		var second = "";
		savecontent variable="second" {
			cfcache( action=arguments.action, key=arguments.key, cachename=variables.cacheName, timespan=createTimespan( 0, 1, 0, 0 ) ) {
				echo( "Hello " & token2 );
			}
		}
		expect( second ).toInclude( "Hello " & token );
		expect( second ).notToInclude( token2 );
	}

}
