component extends="org.lucee.cfml.test.LuceeTestCase" labels="static" {

	function run( testResults, testBox ) {
		describe( "LDEV-4792 call a closure stored as a final member of the static scope", function() {

			it( title="static.lambda() inside a static function", body=function( currentSpec ) {
				expect( LDEV4792.StaticClosure::callLambda() ).toBe( "lambda" );
			});

			it( title="static.closure() with positional and named arguments", body=function( currentSpec ) {
				expect( LDEV4792.StaticClosure::callClosure() ).toBe( "closure:positional" );
				expect( LDEV4792.StaticClosure::callClosureNamed() ).toBe( "closure:named" );
			});

			it( title="Component::lambda() from outside the component", body=function( currentSpec ) {
				expect( LDEV4792.StaticClosure::lambda() ).toBe( "lambda" );
				expect( LDEV4792.StaticClosure::closure( a = "outside" ) ).toBe( "closure:outside" );
			});

			it( title="reading the member and calling a non final closure still work", body=function( currentSpec ) {
				expect( LDEV4792.StaticClosure::readLambda() ).toBe( "lambda" );
				expect( LDEV4792.StaticClosure::callNotFinal() ).toBe( "notFinal" );
			});

		});
	}
}
