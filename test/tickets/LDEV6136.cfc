<cfscript>
component extends="org.lucee.cfml.test.LuceeTestCase" labels="cache,ehCache" {

	function run( testResults, testBox ) {

		describe( "LDEV-6136 extension bundle name backwards compat", function() {

			xit( "works with old bundle name: ehcache.extension", function() {
				var cacheName = "ldev6136old";
				application action="update"
					caches="#{ "#cacheName#": cacheConfig( "ehcache.extension" ) }#";
				cachePut( id: "testOld", value: "old", cacheName: cacheName );
				expect( cacheGet( id: "testOld", cacheName: cacheName ) ).toBe( "old" );
			});

			it( "works with new bundle name: org.lucee.ehcache.extension", function() {
				var cacheName = "ldev6136new";
				application action="update"
					caches="#{ "#cacheName#": cacheConfig( "org.lucee.ehcache.extension" ) }#";
				cachePut( id: "testNew", value: "new", cacheName: cacheName );
				expect( cacheGet( id: "testNew", cacheName: cacheName ) ).toBe( "new" );
			});

		});

	}

	private struct function cacheConfig( required string bundleName ) {
		return {
			class: 'org.lucee.extension.cache.eh.EHCache'
			, bundleName: arguments.bundleName
			, storage: false
			, custom: { "distributed": "off", "maxelementsinmemory": "1000" }
			, default: ''
		};
	}

}
</cfscript>
