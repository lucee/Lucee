component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( "Testcase for LDEV-3533", function() {
			application name="LDEV-3533" regex={"type":"java"}; // sets java as regex engine
			it(title = "reFindnocase() with java regex engine", body = function( currentSpec ) {
				expect(arrayLen(reFindnocase("(f)(oo)", "foo", 1, true, "all")[1].len)).toBe(3);
				expect(reFindnocase("(f)(oo)", "foo", 1, false, "all")[1]).toBe(1);
				expect(arrayLen(reFindnocase("(f)(oo)", "foo", 1, true, "one").len)).toBe(3);
				expect(reFindnocase("(f)(oo)", "foo", 1, false, "one")).toBe(1);
			});
		});
		describe( "Testcase for LDEV-3690", function() {
			application name="LDEV-3690" regex={"type":"java"};
			it(title = "reFindnocase start argument with java regex engine", body = function( currentSpec ) {
				var result = reFindnocase("[1-9]+", "123abc456", 6, true);
				expect(result.pos[1]).toBe(7);
				expect(result.match[1]).toBe(456);
				expect(reFindnocase("[1-9]+", "123abc456", 6, false)).toBe(7);
			});
			it(title = "rematchNoCase() & reReplaceNoCase() with java regex engine", body = function( currentSpec ) {
				expect(rematchNoCase("[a-z]+","abcABC")[1]).toBe("abcABC");
				expect(reReplaceNoCase( "ONE123", "[a-z]+", "", "one")).toBe("123");
				expect(reReplaceNoCase( "ONE123ONE", "[a-z]+", "", "one")).toBe("123ONE");
				expect(reReplaceNoCase( "ONE123ONE", "[a-z]+", "", "all")).toBe("123");
			});
		});
	}
}