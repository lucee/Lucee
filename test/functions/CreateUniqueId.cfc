component extends="org.lucee.cfml.test.LuceeTestCase" {
	function run( testResults , testBox ) {
		describe( title="Test suite for CreateUniqueId()", body=function() {

			it(title="checking CreateUniqueId() function", body = function( currentSpec ) {
				expect( CreateUniqueId() ).notToBe( CreateUniqueId() );
				expect( len( CreateUniqueId() ) ).toBe( 22 );
			});

			it(title="checking CreateUniqueId('counter') function", body = function( currentSpec ) {
				expect( CreateUniqueId( "counter" ) ).notToBe( CreateUniqueId( "counter" ) );
			});

		});
	}
}
