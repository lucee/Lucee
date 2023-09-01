component extends="org.lucee.cfml.test.LuceeTestCase"{
	function beforeAll(){
		
	}

	function run( testResults , testBox ) {
		describe( "Test suite for type casting", function() {
			it( title='convert string to number at runtime', body=function( currentSpec ) {
				var zero=0; // Micha: i set this to a variable instead of setting the number directly into the operaton, because otherwise the compiler will already optimize
				expect(".1"+zero).toBe(0.1);
				expect("1."+zero).toBe(1);
				expect("1.1"+zero).toBe(1.1);
			});
			it( title='convert string to number at compile time', body=function( currentSpec ) {
				expect(".1"+0).toBe(0.1);
				expect("1."+0).toBe(1);
				expect("1.1"+0).toBe(1.1);
			});
			
		});
	}

}	