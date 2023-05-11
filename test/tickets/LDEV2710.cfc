component extends="org.lucee.cfml.test.LuceeTestCase"{

	function run( testResults , testBox ) {
		describe( "test case for LDEV-2710", function() {
			it( title="ListFind with includeEmptyFields", body=function( currentSpec ) {
				
				expect(listFind("1,,apple,orange,,banana,,,","",",",true)).toBe(2);
				expect(listFind(",,apple,orange,,banana,,,","",",",true)).toBe(1);
				expect(listFind("apple,orange,banana,,,","",",",true)).toBe(4);
			});

			it( title="ListFindNoCase with includeEmptyFields", body=function( currentSpec ) {

				expect(listFindNoCase("1,,apple,orange,,banana,,,","",",",true)).toBe(2);
				expect(listFindNoCase(",,apple,orange,,banana,,,","",",",true)).toBe(1);
				expect(listFindNoCase("apple,orange,banana,,,","",",",true)).toBe(4);
			});

		});
	}

}