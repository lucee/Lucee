component extends="org.lucee.cfml.test.LuceeTestCase"{

	function run( testResults , testBox ) {

		describe( title="Test suite for Ismpty()", body=function() {

			it(title="checking IsEmpty() function", body = function( currentSpec ) {
				expect( isEmpty( 1 ) ).toBeFalse();
				expect( isEmpty( 0 ) ).toBeFalse();
				expect( isEmpty( false ) ).toBeFalse();
				expect( isEmpty( true ) ).toBeFalse();
				expect( isEmpty( " " ) ).toBeFalse();
				expect( isEmpty( "" ) ).toBeTrue();

				expect( isEmpty( [] ) ).toBeTrue();
				expect( isEmpty( [1] ) ).toBeFalse();

				expect( isEmpty( {} ) ).toBeTrue();
				expect( isEmpty( { notEmpty: true } ) ).toBeFalse();

				expect( isEmpty( QueryNew( 'mt' ) ) ).toBeTrue();
				expect( isEmpty( queryNew( "name", "varchar", [ [ "Susi" ] ] ) ) ).toBeFalse();
			});

		});
	}

}
