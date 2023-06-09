component extends="org.lucee.cfml.test.LuceeTestCase" labels="date" skip=false {

	function run( testResults , testBox ) {

		describe( "test case for LDEV-4506", function() {
			it( title=" '5 6' should not be treated as a numeric", body=function( currentSpec ) {
				expect(function(){
					toNumeric("5 6");
				}).tothrow();
			});

			it( title=" '5 6' should not be treated as a date",skip=true, body=function( currentSpec ) { // MICHA: skiping this,because we wanna Lucee to convert this to a date
				expect( isDate("5 6") ).toBeFalse();
			});
		});

	}

}