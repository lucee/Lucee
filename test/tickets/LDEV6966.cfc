component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults, testBox ) {
		describe( "LDEV-6966 RAM fallback must register CacheResourceProvider under scheme ram, not tar", function() {

			it( title="CacheResourceProvider is registered under ram and never under tar", body=function() {
				var schemes = {};
				loop array=getPageContext().getConfig().getResourceProviders() item="local.provider" {
					schemes[ provider.getScheme() ] = provider.getClass().getName();
				}
				expect( schemes ).toHaveKey( "ram", "ram scheme should be registered" );
				expect( schemes.ram ).toBe( "lucee.commons.io.res.type.cache.CacheResourceProvider" );
				if ( structKeyExists( schemes, "tar" ) ) {
					expect( schemes.tar ).notToBe( "lucee.commons.io.res.type.cache.CacheResourceProvider", "CacheResourceProvider must not be registered under tar" );
				}
			});

		});
	}

}
