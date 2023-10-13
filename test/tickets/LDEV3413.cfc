component extends="org.lucee.cfml.test.LuceeTestCase" {
	function run( testResults , testBox ) {
		describe( title = "Testcase for LDEV3413, deserializeJSON() for an  empty string ", body = function() {
			it( title = "Testcase for LDEV3413, deserializeJSON() for an empty string", body = function( currentSpec ) {
				expect(deserializeJSON('""')).toBe("");
				expect( function(){
					deserializeJSON("") 
				}).toThrow();
				details = deserializeJSON( '{"company":"mitrahSoft"}' );
				expect(details.company).toBe("mitrahSoft");
			});
		});
	}
}