component extends="org.lucee.cfml.test.LuceeTestCase"{
	function TestInteger(required integer myID) {
		return myID;
	}
 	function run( testResults , testBox ) {
 		describe( title="Test suite for LDEV-1669", body=function() {
 			it(title="Checking argument type is an integer", body = function( currentSpec ) {
 				try{
 					result = TestInteger(8);
 				} catch ( any e ) {
					result = e.message;
				}
 				expect(result).toBe('8');
 			});
 		});
 	}
 }
