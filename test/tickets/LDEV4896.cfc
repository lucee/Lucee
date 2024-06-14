component extends="org.lucee.cfml.test.LuceeTestCase" skip="true" {
	function test() {
		return "abc";
	}
	function run( testResults, testBox ) {
		describe("Testcase for LDEV-4896 - can't compare complex object types as simple value", function() {
			it( title="compare functions !==", body=function( currentSpec ) {
			   expect( function(){
					if ( test !== test )
						return true;
				}).notToThrow(); 
			});
			it( title="compare functions !=", body=function( currentSpec ) {
				expect( function(){
					if ( test != test )
						return true;
				}).notToThrow();
			});
			it( title="compare functions ==", body=function( currentSpec ) {
				expect( function(){
					if ( test == test )
						return true;
			  	}).notToThrow();
			});
			it( title="compare functions ===", body=function( currentSpec ) {
				expect( function(){
					if ( test === test )
						return true;
				}).notToThrow();
			});
		}); 
	}
}