component extends="org.lucee.cfml.test.LuceeTestCase" labels="parser" {

	function run( testResults, testBox ) {
		describe( "LDEV-2489 call a function stored in an array directly", function() {

			it( title="myarray[ 1 ]() calls the closure stored in the array", body=function( currentSpec ) {
				include "LDEV2489/script.cfm";
				expect( request.ldev2489.first ).toBe( 1 );
				expect( request.ldev2489.second ).toBe( 42 );
				expect( request.ldev2489.named ).toBe( 10 );
				expect( request.ldev2489.dynamic ).toBe( 8 );
			});

			it( title="array member functions still work", body=function( currentSpec ) {
				include "LDEV2489/script.cfm";
				expect( request.ldev2489.member ).toBe( 2 );
			});

			it( title="calling an index that holds no function still throws", body=function( currentSpec ) {
				var arr = [ 1, 2 ];
				expect( function() {
					arr[ 1 ]();
				}).toThrow();
				expect( function() {
					arr[ 5 ]();
				}).toThrow();
			});

		});
	}

}
