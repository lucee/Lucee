component extends="org.lucee.cfml.test.LuceeTestCase"{
	//function beforeAll(){}

	private function check(name){
		try{
			include "LDEV2007/"&name&".cfm";
			return true;
		}
		catch(e){
			//systemOutput(e.message,1,1);
			return false;
		}
	}

	function run( testResults , testBox ) {
		describe( "Test case for LDEV-2007", function() {
			it(title = "Checking index loop with invalid attribute(endRow) ", body = function( currentSpec ) {
				expect(check("case1")).toBe(false);
			});
			it(title = "Checking condition loop with invalid attribute(delimiters) ", body = function( currentSpec ) {
				expect(check("case2")).toBe(false);
			});
			it(title = "Checking array loop with invalid attribute(startRow) ", body = function( currentSpec ) {
				expect(check("case3")).toBe(false);
			});
			it(title = "Checking collection loop with invalid attribute(step) ", body = function( currentSpec ) {
				expect(check("case4")).toBe(false);
			});
			it(title = "Checking list loop with invalid attributes(from and to) ", body = function( currentSpec ) {
				expect(check("case5")).toBe(false);
			});
			it(title = "Checking query loop with invalid attribute(item) ", body = function( currentSpec ) {
				expect(check("case6")).toBe(false);
			});
		});
	}

}
