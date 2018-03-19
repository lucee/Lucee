component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( "Test suite for LDEV-1705", function() {
			it( title='checking ArrayPrepend(), with merge attribute true', body=function( currentSpec ) {
				var myArray = [1,2,3,4,5];
				var hasError = false;
				try{
					Arrayprepend(myArray,[6,7], true);
				} catch ( any e ){
					hasError = true;
				}
				assertEquals(false, hasError);
				assertEquals("[6,7,1,2,3,4,5]", serializeJSON(myArray));
				try{
					Arrayprepend(myArray,8, true);
				} catch ( any e ){
					hasError = true;
				}
				assertEquals("[8,6,7,1,2,3,4,5]", serializeJSON(myArray));
			});

			it( title='checking ArrayPrepend(), with merge attribute false', body=function( currentSpec ) {
				var myArray = [1,2,3,4,5];
				var hasError = false;
				try{
					Arrayprepend(myArray,[6,7], false);
				} catch ( any e ){
					hasError = true;
				}
				assertEquals(false, hasError);
				assertEquals("[[6,7],1,2,3,4,5]", serializeJSON(myArray));
				try{
					Arrayprepend(myArray,8, false);
				} catch ( any e ){
					hasError = true;
				}
				assertEquals("[8,[6,7],1,2,3,4,5]", serializeJSON(myArray));
			});
		});
	}
}