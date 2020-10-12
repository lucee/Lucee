component extends = "org.lucee.cfml.test.LuceeTestCase" {

	function run ( testResults , testBox ) {
		describe( "Test case for LDEV-3087", function(){
			it( title = "Checking the reRePlaceNocase function", body = function( currentSpec ){
				expect(reRePlaceNocase('aaaaa','a','b','all')).toBe("bbbbb");
				expect(reRePlaceNocase('and "ID" = ?', "and\s|or\s", "", "one")).toBe('"ID" = ?');
				expect(reReplaceNoCase('ABCDE','[a-zA-Z]',"a",'all')).toBe("aaaaa")
				expect(reRePlaceNocase('AAaaa','a','b','all')).toBe("bbbbb");
				expect(reRePlaceNocase('AND "ID" = ?', "and\s|or\s", "", "one")).toBe('"ID" = ?');
				expect(reReplaceNoCase('ABCDE','[a-z]',"a",'all')).toBe("aaaaa");
			});
		});
	}
}