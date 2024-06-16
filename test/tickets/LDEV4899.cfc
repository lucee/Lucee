component extends="org.lucee.cfml.test.LuceeTestCase" labels="syntax" skip=true{
	
	function run( testResults, testBox ) {
		describe("Testcase for LDEV-4899 - compiler crash", function() {
			it( title="lucee.runtime.type.Closure not found by org.objectweb.asm", body=function( currentSpec ) {
				expect( function(){
					//var prop = args.prop  ?: function(){ return "" };
				}).notToThrow();
			});
		}); 
	}
}