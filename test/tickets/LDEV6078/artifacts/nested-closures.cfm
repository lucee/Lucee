<cfscript>
// LDEV-6078: Nested closures (TestBox/BDD style)
// Bug: nested function() {} closures may have line number issues

function runTests() {
	var results = [];

	describe( "Outer suite", function() {
		beforeAll( function() {
			results.append( "beforeAll" );
		} );

		it( "should run first test", function() {
			results.append( "test1" );
		} );

		describe( "Inner suite", function() {
			it( "should run nested test", function() {
				results.append( "nested" );
			} );
		} );

		afterAll( function() {
			results.append( "afterAll" );
		} );
	} );

	return results;
}

// Mock BDD functions for testing
function describe( label, body ) { body(); }
function beforeAll( body ) { body(); }
function afterAll( body ) { body(); }
function it( label, body ) { body(); }

systemOutput( runTests().toList(), true );
</cfscript>
