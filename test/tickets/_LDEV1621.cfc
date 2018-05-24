component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( "Test suite for LDEV-1621", function() {
			it( title='checking lambda expression, without curly braces in arguments', body=function( currentSpec ) {
				myList = [1, 2, 3];
			    fn = arrayToList(myList.map((x) => x * 3));
				expect(fn).toBe('3,6,9');
			});

			it( title='checking lambda expression, Using curly braces in arguments', body=function( currentSpec ) {
				myList = [1, 2, 3];
				fn = arrayToList(myList.map((x) => {x * 3}));
				expect(fn).toBe('3,6,9');
			});

			it( title='checking lambda expression, Using return keyword with braces in arguments', body=function( currentSpec ) {
				myList = [1, 2, 3];
				fn = arrayToList(myList.map((x) => {return x * 3}));
				expect(fn).toBe('3,6,9');
			});

			it( title='checking lambda expression, Using Multiple Expressions with curly braces', body=function( currentSpec ) {
				myList = [1, 2, 3];
				fn = arrayToList(myList.map((x) => { var n = 3; x * n }));
				expect(fn).toBe('3,6,9');
			});
			it( title='checking lambda expression, Using Multiple Expressions without curly braces', body=function( currentSpec ) {
				myList = [1, 2, 3];
				n = 3 ;
				fn = arrayToList(myList.map((x) => x * n));
				expect(fn).toBe('3,6,9');
			});
		});
	}
}
