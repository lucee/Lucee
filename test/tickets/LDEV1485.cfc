component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( "Test suite for LDEV-1485", function() {
			it( title='Checking ArrayFilter(), without arguments in udf call', body=function( currentSpec ) {
			   local.result = doSomething('foo,bar');
				expect(local.result[1]).toBe('foo,bar');
			});

			it( title='Checking ArrayFilter(), with arguments in udf call', body=function( currentSpec ) {
				local.result = doSomething2('foo,bar');
				expect(local.result[1]).toBe('foo,bar');
			});
		});
	}

	function doSomething() {
	   ArrayFilter1 = ArrayFilter( arguments, function() {
	       return true;
	   });
	  return ArrayFilter1;
	}

	function doSomething2(foo,bar) {
	   ArrayFilter2 = ArrayFilter( arguments, function() {
	       return true;
	   });
	  return ArrayFilter2;
	}
}