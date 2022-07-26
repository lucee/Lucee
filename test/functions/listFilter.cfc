component extends = "org.lucee.cfml.test.LuceeTestCase" {
	function run( testResults, textbox ) {
		describe("testcase for listFilter()", function() {
			variables.list = "one,two,three";
			it(title="checking listFilter() function", body=function( currentSpec ) {
				callback=function(elem){ 
					return elem!="two"; 
				}
				assertEquals('one,three', listFilter( list, callback ));
			});
			it(title="checking list.listFilter() member function", body=function( currentSpec ) {
				callback=function(elem){ 
					return elem!="two"; 
				} 
				assertEquals('one,three', list.listFilter( callback ));
			});
		});
	}
}