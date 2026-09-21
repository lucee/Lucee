component extends="org.lucee.cfml.test.LuceeTestCase" labels="metadata,component" {

	function run( testResults, testBox ) {
		describe( "LDEV-6469 GetMetadata().functions includes inherited methods for same-named CFCs", function() {

			it( "includes inherited methods and their annotations when child and parent share the same filename", function() {
				// child.Default extends parent.Default — same simple class name, different packages.
				// After LDEV-6056, PageSource.equals() distinguishes those files, so getUDFs()
				// skips parent methods from the child's top-level functions array.
				var uri    = createURI( "LDEV6469/test.cfm" );
				var result = deserializeJSON( _InternalRequest( template: uri ).fileContent.trim() );

				expect( result.functionNames ).toInclude( "layout",
					"child's own layout() should be in GetMetadata().functions; found: #result.functionNames.toList()#" );
				expect( result.functionNames ).toInclude( "render",
					"inherited render() should be in GetMetadata().functions; found: #result.functionNames.toList()#" );
				expect( isBoolean( result.renderCacheable ) && !result.renderCacheable ).toBeTrue(
					"inherited render() should retain @cacheable false; got: #serializeJSON( result.renderCacheable )#" );
			});

		});
	}

	private string function createURI( string calledName ) {
		var baseURI = "/test/#listLast( getDirectoryFromPath( getCurrentTemplatePath() ), "\/" )#/";
		return baseURI & arguments.calledName;
	}

}
