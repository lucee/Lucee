component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults, testBox ) {
		describe( "LDEV-6056 super.method() with same-named components in different packages", function() {

			it( "can call super.method() when child and parent have same simple name", function() {
				// preside.InterceptorService extends coldbox.InterceptorService
				// Both have the simple name "InterceptorService"
				// PageSource.equals() must use full path, not just simple name
				var uri = createURI( "LDEV6056/test.cfm" );
				var result = _InternalRequest( template: uri ).fileContent.trim();
				expect( result ).toBe( "preside.InterceptorService -> coldbox.InterceptorService.registerInterceptor" );
			});

		});
	}

	private string function createURI( string calledName ) {
		var baseURI = "/test/#listLast( getDirectoryFromPath( getCurrentTemplatePath() ), "\/" )#/";
		return baseURI & arguments.calledName;
	}

}
