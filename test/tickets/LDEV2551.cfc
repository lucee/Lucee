component extends="org.lucee.cfml.test.LuceeTestCase" labels="syntax" {

	function beforeAll() {
		variables.uri = createURI( "LDEV2551" );
	}

	// returns the content, or the message of the error thrown by the template
	private string function run_( required string file ) {
		try {
			return _InternalRequest( template: "#variables.uri#/#arguments.file#" ).fileContent.trim();
		}
		catch ( any e ) {
			return "ERR:" & e.message;
		}
	}

	function run( testResults, testBox ) {

		describe( "LDEV-2551 final variables", function() {

			it( "a final variable in a template can be read, mutated internally, and final stays usable as a name", function() {
				expect( run_( "template.cfm" ) ).toBe( "1,A,B,5,9,2,7,8,4" );
			});

			it( "reassigning a final variable in a template throws", function() {
				for ( var f in [ "err-reassign", "err-plus", "err-incr", "err-preincr", "err-concat", "err-redeclare", "err-times" ] ) {
					var r = run_( "#f#.cfm" );
					expect( r ).toInclude( "ERR:" );
					expect( r ).toInclude( "final variable [x]" );
				}
			});

			it( "reassigning a final variable in a function throws, with and without var", function() {
				expect( run_( "err-function.cfm" ) ).toInclude( "final variable [x]" );
				expect( run_( "err-function-noVar.cfm" ) ).toInclude( "final variable [x]" );
			});

			it( "final in functions does not affect other variables, closures or the template scope", function() {
				expect( run_( "function-ok.cfm" ) ).toBe( "132,511,2,100" );
			});

			it( "only simple variable names can be final", function() {
				expect( run_( "err-name.cfm" ) ).toInclude( "ERR:" );
			});

			it( "final in components keeps working", function() {
				var c = new LDEV2551.Comp();
				expect( c.ok() ).toBe( 1 );
				expect( function() { c.set( 5 ); } ).toThrow( message="final" );
				expect( function() { c.setB( 5 ); } ).toThrow( message="final" );
				expect( function() { new LDEV2551.CompBad(); } ).toThrow( message="final variable [t]" );
			});

		});
	}

	private string function createURI( string calledName ) {
		var baseURI = "/test/#listLast( getDirectoryFromPath( getCurrenttemplatepath() ), "\/" )#/";
		return baseURI & "" & calledName;
	}

}
