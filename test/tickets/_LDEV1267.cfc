component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( "Test suite for LDEV-1267", function() {
			it( title='Checking null value variable in structure', body=function( currentSpec ) {
			 	testNull = javacast( 'null', '' );
				testStruct = { a: testNull };
				assertEquals( javacast( 'null', '' ), testStruct.a );
			});

			it( title='Checking null value variable in array', body=function( currentSpec ) {
			 	testNull = javacast( 'null', '' );
				testArray = [ testNull ];
				assertEquals( javacast( 'null', '' ), testArray[ 1 ] );
			});

			it( title='Checking a null value stored in structure', body=function( currentSpec ) {
				testStruct = { a: javacast( 'null', '' ) };
				assertEquals( javacast( 'null', '' ), testStruct.a );
			});

			it( title='Checking a null value stored in array', body=function( currentSpec ) {
				testArray = [ javacast( 'null', '' ) ];
				assertEquals( javacast( 'null', '' ), testArray[ 1 ] );
			});
		});
	}
}