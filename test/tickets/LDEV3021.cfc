component extends = "org.lucee.cfml.test.LuceeTestCase" {

	function run ( testResults , testBox ) {
		describe( "Test case for LDEV-3021", function(){
			
			it( title = "Checking the reReplaceNoCase function", body = function( currentSpec ){
				expect(reReplaceNoCase('AAaaa','a','b','all')).toBe('bbbbb');
			});
		});
	}
}


