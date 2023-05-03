component extends="org.lucee.cfml.test.LuceeTestCase" labels="syntax" {
	function run( testResults , testBox ) {
		describe( title="Test suite for LDEV-1522", body=function() {
			it( title='Checking elvis operator using built-in-function, result from variable', body=function( currentSpec ) {
				var reqHeaders = getHTTPRequestData().headers;
				expect(reqHeaders.origin ?: "undefined").toBe("undefined");
			});

			it( title='Checking elvis operator using built-in-function directly', skip=true, body=function( currentSpec ) {
				expect(getHTTPRequestData().headers.origin ?: "undefined").toBe("undefined");
			});

			it( title='Checking elvis operator using built-in-function directly (missing top level key, with subkey)', skip=true, body=function( currentSpec ) {
				expect(getHTTPRequestData().missing.origin ?: "undefined").toBe("undefined");
			});

			it( title='Checking elvis operator using built-in-function directly (missing top level key)', skip=true, body=function( currentSpec ) {
				expect(getHTTPRequestData().missing ?: "undefined").toBe("undefined");
			});

			it( title='Checking elvis operator using UDF, on result in variable', body=function( currentSpec ) {
				var result= test();
				expect(result.foo2 ?: "undefined").toBe("undefined");
			});

			it( title='Checking elvis operator using UDF, on result from function', skip=true, body=function( currentSpec ) {
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
