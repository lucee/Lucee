component extends="org.lucee.cfml.test.LuceeTestCase" labels="resource" {

	function afterAll() {
		structDelete( server, "LDEV3292_last" );
	}

	function run( testResults, testBox ) {
		describe( "LDEV-3292 CFML resource provider runs in the PageContext of the calling request", function() {

			it( title="the component is found with the mappings of the caller", body=function( currentSpec ) {
				var provider = newProvider();
				expect( provider.getResource( "/foo.txt" ) ).notToBeNull();
			});

			it( title="getResource() sees the request and application scope of the caller", body=function( currentSpec ) {
				var provider = newProvider();
				request.LDEV3292_marker = createUUID();
				request.LDEV3292_calls = [];
				structDelete( server, "LDEV3292_last" );
				provider.getResource( "/foo.txt" );
				provider.getResource( "/bar.txt" );
				expect( server.LDEV3292_last.path ).toBe( "/bar.txt" );
				expect( server.LDEV3292_last.requestMarker ).toBe( request.LDEV3292_marker );
				expect( server.LDEV3292_last.appName ).toBe( getApplicationSettings().name );
				expect( request.LDEV3292_calls ).toBe( [ "/foo.txt", "/bar.txt" ] );
			});
		});
	}

	private function newProvider() {
		var args = createObject( "java", "java.util.HashMap" ).init();
		args.put( "component", "test.tickets.LDEV3292.Provider" );
		var provider = createObject( "java", "lucee.commons.io.res.type.cfml.CFMLResourceProvider" ).init();
		provider.init( "ldevthreetwo", args );
		return provider;
	}
}
