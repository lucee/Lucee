component extends="org.lucee.cfml.test.LuceeTestCase" {
	function run( testResults, testBox ){
		describe( title="Testcase for numberFormat()", body=function() {
			it( title = "Checking with numberFormat()", body=function( currentSpec ) {
				assertEquals('"00123"',serializeJSON(numberFormat(123,'00000')));
				assertEquals('"+123"',serializeJSON(numberFormat(123,'+')));
				assertEquals('"-123"',serializeJSON(numberFormat(-123,'+')));
				assertEquals('"123.00"',serializeJSON(numberFormat(123,'__.00')));
				assertEquals('"11.10"',serializeJSON((numberFormat(11.1,'__.00'))));
			});
			it( title="Checking with Numeric.numberFormat() member function", body=function( currentSpec ) {
				var num = 123
				assertEquals('"00123"',serializeJSON(num.numberFormat('00000')));
				assertEquals('"+123"',serializeJSON(num.numberFormat('+')));
				assertEquals('"123.00"',serializeJSON(num.numberFormat('__.00')));
			});
		});
	}
}