component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( "Test suite for LDEV-1484", function() {
			it( title='checking Lamdas expression, without using curly braces in arguments', body=function( currentSpec ) {
			   fn = (x) => arguments
			   fn = (fn("hello world"));
				expect(fn.x).toBe('hello world');
			});

			it( title='checking Lamdas expression, Using return keyword with braces in arguments', body=function( currentSpec ) {
				fn = (y) => {return arguments;}
			    fn = (fn("hello world2"));
				expect(fn.y).toBe('hello world2');
			});

			it( title='checking Lamdas expression, Using curly braces in arguments', body=function( currentSpec ) {
				fn = (z) => {arguments;}
			   	fn = (fn("hello world3"));
				expect(fn.z).toBe('hello world3');
			});
		});
	}
}