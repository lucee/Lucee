component extends="org.lucee.cfml.test.LuceeTestCase"{

	function run( testResults , testBox ) {
		describe( "test case for LDEV2729", function() {
			it( title="replacelist with includeemptyfields in 4th/5th/6th position", body=function( currentSpec ) {
				
				expect(replacelist( "{name}", "{,}", ",", false)).toBe("name");
				expect(replacelist( "{name}", "{,}", ",", true)).toBe("name");
				expect(replacelist( "{name}", "{,}", ",", ",", true)).toBe("name");
				expect(replacelist( "{name}", "{,}", ",", ",", false)).toBe("name");
			});

			it( title="replacelist with includeemptyfields in correct position", body=function( currentSpec ) {
				
				expect(replacelist( "{name}", "{,}", ",", ",", ",", false)).toBe("name");
				expect(replacelist( "{name}", "{,}", ",", ",", ",", true)).toBe("name");
				expect(replacelist( "{name,,}", "{,}", ",", ",", ",", false)).toBe("name,,");
				expect(replacelist( "{name,,}", "{,}", ",", ",", ",", true)).toBe("name,,");
			});

		});

		describe( "test case for LDEV2729", function() {
			it( title="ReplaceListNoCase with includeemptyfields in 4th/5th/6th position", body=function( currentSpec ) {
				
				expect(ReplaceListNoCase( "{name}", "{,}", ",", false)).toBe("name");
				expect(ReplaceListNoCase( "{name}", "{,}", ",", true)).toBe("name");
				expect(ReplaceListNoCase( "{name}", "{,}", ",", ",", true)).toBe("name");
				expect(ReplaceListNoCase( "{name}", "{,}", ",", ",", false)).toBe("name");
			});

			it( title="ReplaceListNoCase with includeemptyfields in correct position", body=function( currentSpec ) {
				
				expect(ReplaceListNoCase( "{name}", "{,}", ",", ",", ",", false)).toBe("name");
				expect(ReplaceListNoCase( "{name}", "{,}", ",", ",", ",", true)).toBe("name");
				expect(ReplaceListNoCase( "{name,,}", "{,}", ",", ",", ",", false)).toBe("name,,");
				expect(ReplaceListNoCase( "{name,,}", "{,}", ",", ",", ",", true)).toBe("name,,");
			});

		});
	}

}
