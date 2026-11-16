component extends="org.lucee.cfml.test.LuceeTestCase" labels="resource" {

	function run( testResults, testBox ) {
		describe( "LDEV-3291 init() arguments of a CFML resource provider", function() {

			it( title="are a case insensitive CFML struct", body=function( currentSpec ) {
				structDelete( request, "LDEV3291_init" );
				var provider = newProvider();
				// isCaseSensitive() loads the component (and calls its init) in the current request
				expect( provider.isCaseSensitive() ).toBeTrue();
				var res = request.LDEV3291_init;
				expect( res.isStruct ).toBeTrue();
				expect( res.lowerKey ).toBeTrue();
				expect( res.upperKey ).toBeTrue();
				expect( res.value ).toBe( "abc" );
				expect( res.keys ).toBe( "component,myArg" );
			});
		});
	}

	private function newProvider() {
		var args = createObject( "java", "java.util.HashMap" ).init();
		args.put( "component", "test.tickets.LDEV3291.Provider" );
		args.put( "myArg", "abc" );
		var provider = createObject( "java", "lucee.commons.io.res.type.cfml.CFMLResourceProvider" ).init();
		provider.init( "ldevthreetwoninetyone", args );
		return provider;
	}
}
