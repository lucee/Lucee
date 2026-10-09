component extends="org.lucee.cfml.test.LuceeTestCase" labels="cache" {

	variables.cacheName = "ldev6546";
	variables.filterClass = "lucee.commons.io.cache.CacheEntryFilter";
	variables.allKeys = [ "k1", "k2", "k3", "k4", "k5" ];

	function beforeAll() {
		var caches = {};
		caches[ variables.cacheName ] = {
			class: "lucee.runtime.cache.ram.RamCache",
			storage: false,
			default: "query",
			custom: { "timeToIdleSeconds": 60, "timeToLiveSeconds": 60 }
		};
		application action="update" caches=caches;
	}

	function afterAll() {
		try {
			cacheClear( cacheName=variables.cacheName );
		}
		catch ( any e ) {}
	}

	function run( testResults, testBox ) {
		describe( "LDEV-6546 cache entry filters and RamCache tag clears", function() {

			it( title="remove(CacheEntryFilter) skips an entry that vanished after keys()", body=function( currentSpec ) {
				var cache = newCache();
				var f = newFilter( cache );
				var removedCount = cache.remove( f.proxy );
				expect( f.filter.wasNullSeen() ).toBeFalse( "filter received a null entry" );
				expect( cache.keys() ).toBeEmpty();
				expect( removedCount ).toBeGT( 0 );
			});

			it( title="keys(CacheEntryFilter) skips an entry that vanished after keys()", body=function( currentSpec ) {
				var cache = newCache();
				var f = newFilter( cache );
				var result = cache.keys( f.proxy );
				expect( f.filter.wasNullSeen() ).toBeFalse( "filter received a null entry" );
				expect( arrayLen( result ) ).toBeLT( arrayLen( variables.allKeys ) );
			});

			it( title="values(CacheEntryFilter) skips an entry that vanished after keys()", body=function( currentSpec ) {
				var cache = newCache();
				var f = newFilter( cache );
				var result = cache.values( f.proxy );
				expect( f.filter.wasNullSeen() ).toBeFalse( "filter received a null entry" );
				expect( arrayLen( result ) ).toBeLT( arrayLen( variables.allKeys ) );
			});

			it( title="entries(CacheEntryFilter) skips an entry that vanished after keys()", body=function( currentSpec ) {
				var cache = newCache();
				var f = newFilter( cache );
				var result = cache.entries( f.proxy );
				expect( f.filter.wasNullSeen() ).toBeFalse( "filter received a null entry" );
				expect( arrayLen( result ) ).toBeLT( arrayLen( variables.allKeys ) );
			});

			it( title="RamCache remove(CacheEntryFilter) passes the stored entry, not a decoupled copy", body=function( currentSpec ) {
				var cache = newCache();
				cache.decouple();
				// decouple() applies to puts too, so add the entries afterwards
				cache.clear();
				for ( var k in variables.allKeys ) cache.put( k, { "key": k }, javacast( "null", "" ), javacast( "null", "" ) );

				var first = newFilter( cache, false, false );
				cache.remove( first.proxy );
				var second = newFilter( cache, false, false );
				cache.remove( second.proxy );

				expect( arrayLen( first.filter.getHashes() ) ).toBe( arrayLen( variables.allKeys ) );
				var a = duplicate( first.filter.getHashes() );
				var b = duplicate( second.filter.getHashes() );
				arraySort( a, "numeric" );
				arraySort( b, "numeric" );
				expect( b ).toBe( a );
			});

			it( title="query tag clears still remove only matching entries", body=function( currentSpec ) {
				cacheClear( cacheName=variables.cacheName );

				expect( exeQuery().isCached() ).toBeFalse();
				expect( exeQuery().isCached() ).toBeTrue();

				var removed = cacheClear( tags=[ "peter", "ueli" ], cacheName=variables.cacheName );
				expect( removed ).toBe( 0 );
				expect( exeQuery().isCached() ).toBeTrue();

				removed = cacheClear( tags=[ "peter", "urs" ], cacheName=variables.cacheName );
				expect( removed ).toBe( 1 );
				expect( exeQuery().isCached() ).toBeFalse();
			});

		});
	}

	private function newCache() {
		var cache = createObject( "java", "lucee.runtime.cache.ram.RamCache" ).init();
		cache.init( javacast( "long", 0 ), javacast( "long", 0 ), javacast( "int", 60 ) );
		for ( var k in variables.allKeys ) cache.put( k, { "key": k }, javacast( "null", "" ), javacast( "null", "" ) );
		return cache;
	}

	private struct function newFilter( required cache, boolean result=true, boolean removeOther=true ) {
		var cfc = new LDEV6546.RemovingFilter( arguments.cache, variables.allKeys, arguments.result, arguments.removeOther );
		return { filter: cfc, proxy: createDynamicProxy( cfc, [ variables.filterClass ] ) };
	}

	private function exeQuery() {
		var q = query( a: [ 1, 2, 3, 4 ] );
		query name="local.qry" cachedwithin=createTimespan( 0, 0, 0, 30 ) dbtype="query" tags=[ "susi", "urs" ] {
			echo( "select * from q" );
		}
		return local.qry;
	}

}
