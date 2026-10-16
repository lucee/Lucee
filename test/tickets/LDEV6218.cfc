component extends="org.lucee.cfml.test.LuceeTestCase" labels="cache" {

	variables.functionCacheName = "LDEV6218_function";
	variables.httpCacheName = "LDEV6218_http";
	variables.httpbin = server.getTestService( "httpbin" );

	function beforeAll() {
		var caches = {};
		caches[ variables.functionCacheName ] = {
			class: "lucee.runtime.cache.ram.RamCache",
			storage: false,
			default: "function",
			custom: { "timeToIdleSeconds": 86400, "timeToLiveSeconds": 86400 }
		};
		caches[ variables.httpCacheName ] = {
			class: "lucee.runtime.cache.ram.RamCache",
			storage: false,
			default: "http",
			custom: { "timeToIdleSeconds": 86400, "timeToLiveSeconds": 86400 }
		};
		application action="update" caches=caches;
	}

	function afterAll() {
		try {
			cacheClear( cacheName=variables.functionCacheName );
			cacheClear( cacheName=variables.httpCacheName );
		}
		catch ( any e ) {}
	}

	function isHttpbinNotAvailable() {
		return structCount( variables.httpbin ) == 0;
	}

	function run( testResults, testBox ) {
		describe( "LDEV-6218 cacheRemove with cachedWithinId does not work for HTTP cache entries", function() {

			it( title="cacheRemove removes a cachedWithin function entry by its cachedWithinId", body=function( currentSpec ) {
				var firstValue = getCachedValue( 1 );
				expect( getCachedValue( 1 ) ).toBe( firstValue );

				var cacheId = cachedWithinId( getCachedValue, [ 1 ] );
				cacheRemove( ids=cacheId, throwOnError=true, cacheName=cacheGetDefaultCacheName( "function" ) );

				expect( getCachedValue( 1 ) ).notToBe( firstValue );
			});

			it( title="cacheRemove removes a cachedWithin cfhttp entry by its cachedWithinId", skip=isHttpbinNotAvailable(), body=function( currentSpec ) {
				var uuidUrl = "http://#variables.httpbin.server#:#variables.httpbin.port#/uuid";
				var cachedWithin = createTimeSpan( 0, 1, 0, 0 );

				cfhttp( url=uuidUrl, result="local.firstResult", cachedwithin=cachedWithin );
				cfhttp( url=uuidUrl, result="local.cachedResult", cachedwithin=cachedWithin );
				expect( local.cachedResult.filecontent ).toBe( local.firstResult.filecontent );

				var cacheId = cachedWithinId( local.firstResult );
				cacheRemove( ids=cacheId, throwOnError=true, cacheName=cacheGetDefaultCacheName( "http" ) );

				cfhttp( url=uuidUrl, result="local.freshResult", cachedwithin=cachedWithin );
				expect( local.freshResult.filecontent ).notToBe( local.firstResult.filecontent );
			});

		});
	}

	private function getCachedValue( id ) cachedwithin="#createTimeSpan( 0, 1, 0, 0 )#" {
		return createUUID();
	}

}
