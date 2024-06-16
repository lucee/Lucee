component extends="org.lucee.cfml.test.LuceeTestCase" labels="numeric" {
	
	function run( testResults, testBox ) {
		describe("Testcase for LDEV-4897 - The decimal value 0.005 is not close enough to any integer", function() {
			it( title="devide a long", body=function( currentSpec ) {
				expect( function(){
					var x = javacast("long", 0.005);
					var y = x / 1000;
				}).notToThrow();
			});
		}); 
	}
}