component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( "Test suite for LDEV-1102", function() {
			it(title="checking component, with property inside cfscript", body = function( currentSpec ) {
				try {
					var uri = new test.testcases.LDEV1102.test();
					var result = uri.getuserId();
				} catch ( any e){
					var result = e.message;
				}
				expect(result).toBe(1);
			});

			it(title="checking component, with property in tag based", body = function( currentSpec ) {
				try {
					var uri = new test.testcases.LDEV1102.test1();
					var result = uri.getuserId();
				} catch ( any e){
					var result = e.message;
				}
				expect(result).toBe(1);
			});

			it(title="checking component, with property in script based", body = function( currentSpec ) {
				try {
					var uri = new test.testcases.LDEV1102.test2();
					var result = uri.getuserId();
				} catch ( any e){
					var result = e.message;
				}
				expect(result).toBe(1);
			});
		});
	}
}