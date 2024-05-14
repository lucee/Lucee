component extends="org.lucee.cfml.test.LuceeTestCase"{

	function beforeAll(){
		application action="update" NULLSupport=true;
	}

	function afterAll(){
		application action="update" NULLSupport=false;
	}

	function run( testResults , testBox ) {
		describe( "Test suite for LDEV-1267", function() {
			it( title='Checking null value variable in structure', body=function( currentSpec ) {
			 	var testNull = javacast( 'null', '' );
				var testStruct = { a: testNull };
				expect( javacast( 'null', '' ) == testStruct.a ).toBeTrue();
			});

			it( title='Checking null value variable in array', body=function( currentSpec ) {
			 	var testNull = javacast( 'null', '' );
				var testArray = [ testNull ];
				expect( javacast( 'null', '' ) == testArray[ 1 ] ).toBeTrue();
			});

			it( title='Checking a null value stored in structure', body=function( currentSpec ) {
				var testStruct = { a: javacast( 'null', '' ) };
				expect( javacast( 'null', '' ) == testStruct.a ).toBeTrue();
			});

			it( title='Checking a null value stored in array', body=function( currentSpec ) {
				var testArray = [ javacast( 'null', '' ) ];
				expect( javacast( 'null', '' ) == testArray[ 1 ] ).toBeTrue();
			});
		});
	}
}