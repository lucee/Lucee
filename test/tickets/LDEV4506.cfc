component extends="org.lucee.cfml.test.LuceeTestCase" labels="date" skip=true {

	function run( testResults , testBox ) {

		describe( "test case for LDEV-4506", function() {
			it( title=" '5 6' should not be treated as a numeric", body=function( currentSpec ) {
				expect(function(){
					toNumeric("5 6");
				}).tothrow();
			});

			it( title=" '5 6' should not be treated as a date", body=function( currentSpec ) {
				expect( isDate("5 6") ).toBeFalse();
			});
		});

	}

}