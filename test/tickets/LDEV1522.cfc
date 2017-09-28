component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( "Test suite for LDEV-1522", function() {
			it( title='Checking elvis operator using built-in-function, result from Variable Name', body=function( currentSpec ) {
				var reqHeaders = getHTTPRequestData().headers;
				expect(reqHeaders.origin ?: "undefined").toBe("undefined");
			});

			it( title='Checking elvis operator using built-in-function, without any variable name', body=function( currentSpec ) {
				expect(getHTTPRequestData().headers.orgin ?: "undefined").toBe("undefined");
			});

			it( title='Checking elvis operator using UDF, result from Variable Name', body=function( currentSpec ) {
				var result= test();
				expect(result.foo2 ?: "undefined").toBe("undefined");
			});

			it( title='Checking elvis operator using UDF, result from Variable Name', body=function( currentSpec ) {
				expect(test().foo2 ?: "undefined").toBe("undefined");
			});

		});
	}

	private struct function test(){
		var rez = {};
		rez.foo1 = "bar1";
		return  rez
	}
}

