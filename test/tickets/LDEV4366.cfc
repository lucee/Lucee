component extends="org.lucee.cfml.test.LuceeTestCase" {
	function beforeAll() {
		variables.uri = createURI("LDEV4366");
	}
	function run( testResults , testBox ) {
		describe( "Testcase for LDEV-4366", function() {
			it(title = "param name= with an unknown attribute must throw", body = function( currentSpec ) {
				try {
					var result = _InternalRequest( template: uri & "/withHint.cfm" ).fileContent.trim();
				} catch(any e) { result = e.message; }
				expect( result ).notToBe( "hint" );
				expect( result ).toInclude( "hint" ); // error names the attribute
			});
			it(title = "param name= with a random attribute must throw", body = function( currentSpec ) {
				try {
					var result = _InternalRequest( template: uri & "/withFoo.cfm" ).fileContent.trim();
				} catch(any e) { result = e.message; }
				expect( result ).notToBe( "bar" );
				expect( result ).toInclude( "foo" );
			});
			it(title = "param shorthand user=""zac"" still works", body = function( currentSpec ) {
				var result = _InternalRequest( template: uri & "/shorthand.cfm" ).fileContent.trim();
				expect( result ).toBe( "zac" );
			});
		});
	}
	private string function createURI(string calledName) {
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI & "" & calledName;
	}
}
