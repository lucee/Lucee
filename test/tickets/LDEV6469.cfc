component extends="org.lucee.cfml.test.LuceeTestCase" labels="metadata,component" {

	function run( testResults, testBox ) {
		describe( "LDEV-6469 GetMetadata().functions includes inherited methods for same-named CFCs", function() {

			it( "includes inherited methods and their annotations when child and parent share the same filename", function() {
				var uri    = createURI( "LDEV6469/test.cfm" );
				var result = deserializeJSON( _InternalRequest( template: uri ).fileContent.trim() );

				expect( result.functionNames ).toInclude( "layout",
					"child's own layout() should be in GetMetadata().functions; found: #result.functionNames.toList()#" );
				expect( result.functionNames ).toInclude( "render",
					"inherited render() should be in GetMetadata().functions; found: #result.functionNames.toList()#" );
				expect( isBoolean( result.renderCacheable ) && !result.renderCacheable ).toBeTrue(
					"inherited render() should retain @cacheable false; got: #serializeJSON( result.renderCacheable )#" );
			});

			it( "does not flatten inherited methods from a differently named parent into functions", function() {
				var uri    = createURI( "LDEV6469/testNamed.cfm" );
				var result = deserializeJSON( _InternalRequest( template: uri ).fileContent.trim() );

				expect( result.functionNames ).toInclude( "own" );
				expect( result.functionNames ).notToInclude( "frombase" );
				expect( result.extendsFunctionNames ).toInclude( "frombase" );
			});

		});
	}

	private string function createURI( string calledName ) {
		var baseURI = "/test/#listLast( getDirectoryFromPath( getCurrentTemplatePath() ), "\/" )#/";
		return baseURI & arguments.calledName;
	}

}
