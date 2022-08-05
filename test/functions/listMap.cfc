component extends = "org.lucee.cfml.test.LuceeTestCase" {
	function run( testResults, textbox ) {
		describe("testcase for listMap()", function() {
			variables.list = "a,b,c,d,,f";
			it(title="checking listMap() function", body=function( currentSpec ) {
				assertEquals( "aa,ba,ca,da,fa", listMap( list, function(e)  { return e & "a"; })); 
			});
			it(title="checking list.listMap() member function", body=function( currentSpec ) {
				assertEquals( "aa,ba,ca,da,fa", list.listMap( function(e) { return e & "a"; })); 
			});
		});
	}
}