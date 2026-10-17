component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults, testBox ) {
		describe( "LDEV-2213 closures and localMode", function() {

			// modern: the local scope is preferred, but a closure still updates variables it closes over (like ACF)
			it( "modern: closure updates a var of the enclosing function", function() {
				expect( runCase( "modern", "enclosing" ) ).toBe( "Before: [] After: [testcase]" );
			} );
			it( "modern: nested closure updates a var of the outer function", function() {
				expect( runCase( "modern", "nested" ) ).toBe( "counter: 2" );
			} );
			it( "modern: variables[ key ] inside a closure writes the variables scope", function() {
				expect( runCase( "modern", "adam" ) ).toBe( "tahi,whero" );
			} );
			it( "modern: a new unknown name stays local to the closure", function() {
				expect( runCase( "modern", "newName" ) ).toBe( "enclosing local: false, variables: false" );
			} );

			// classic: behaviour unchanged
			it( "classic: closure updates a var of the enclosing function", function() {
				expect( runCase( "classic", "enclosing" ) ).toBe( "Before: [] After: [testcase]" );
			} );
			it( "classic: nested closure updates a var of the outer function", function() {
				expect( runCase( "classic", "nested" ) ).toBe( "counter: 2" );
			} );
			it( "classic: variables[ key ] inside a closure writes the variables scope", function() {
				expect( runCase( "classic", "adam" ) ).toBe( "tahi,whero" );
			} );
			it( "classic: a new unknown name goes to the variables scope", function() {
				expect( runCase( "classic", "newName" ) ).toBe( "enclosing local: false, variables: true" );
			} );
		} );
	}

	private string function runCase( required string mode, required string name ) {
		var result = _InternalRequest(
			template: createURI( "LDEV2213/#arguments.mode#/index.cfm" ),
			url: { case: arguments.name }
		);
		return trim( result.filecontent );
	}

	private string function createURI( string calledName ) {
		var baseURI = "/test/#listLast( getDirectoryFromPath( getCurrentTemplatePath() ), "\/" )#/";
		return baseURI & calledName;
	}
}
