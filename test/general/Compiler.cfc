component extends="org.lucee.cfml.test.LuceeTestCase"{
	function beforeAll(){
		
	}

	function run( testResults , testBox ) {
		describe( "Test suite for compiler", function() {
			it( title='unary operator', body=function( currentSpec ) {
				expect(1--).toBe(1);
				expect(1++).toBe(1);
				expect(--2).toBe(1);
				expect(++0).toBe(1);
			});
			it( title='negate operator', body=function( currentSpec ) {
				var num = 4;
				DateAdd("d", -num, "01/04/2022");
				DateAdd("d", +num, "01/04/2022");
			});
		});
	}

}