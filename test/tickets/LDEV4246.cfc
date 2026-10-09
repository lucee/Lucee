component extends="org.lucee.cfml.test.LuceeTestCase" labels="static" {

	function run( testResults, testBox ) {
		describe("Testcase for LDEV-4246", function() {

			it( title="calling static methods from an abstract component", body=function( currentSpec ) {
				try {
					var result = LDEV4246.abstract::testFunc();
				}
				catch(any e) {
					var result = e.message;
				}
				expect(trim(result)).toBe("static methods from an abstract component");
			});

			it( title="reading a static variable of an abstract component", body=function( currentSpec ) {
				expect( LDEV4246.abstract::NAME ).toBe( "abstract" );
			});

			it( title="an abstract component still can't be instantiated", body=function( currentSpec ) {
				LDEV4246.abstract::testFunc();
				expect( function() {
					new LDEV4246.abstract();
				}).toThrow( message="you cannot instantiate an abstract component" );
			});
		});
	}

}