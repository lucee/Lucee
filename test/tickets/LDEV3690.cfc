component extends="org.lucee.cfml.test.LuceeTestCase"{

	function beforeAll() {
		application name="LDEV-3690" regex={"type":"java"}; // sets java as regex engine
	}

	function run( testResults , testBox ) {
		describe( "Testcase for LDEV-3533", function() {
			it(title = "reFindnocase() with java regex engine", body = function( currentSpec ) {
				expect(arrayLen(reFindnocase("(f)(oo)", "foo", 1, true, "all")[1].len)).toBe(3);
				expect(reFindnocase("(f)(oo)", "foo", 1, false, "all")[1]).toBe(1);
				expect(arrayLen(reFindnocase("(f)(oo)", "foo", 1, true, "one").len)).toBe(3);
				expect(reFindnocase("(f)(oo)", "foo", 1, false, "one")).toBe(1);
			});
		});
		describe( "Testcase for LDEV-3690", function() {
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
			it(title = "reFind() with java regex engine", body = function( currentSpec ) {
				// check this (from LDEV-3103) because once it throws String index out of range: -1 with java regex engine
				var text="
```
echo( x );
```
";
				var text = Replace(text, "#chr(13)##chr(10)#",chr(10), "all");
    			var referenceRegex = "```([a-z\+]+)?\n(.*?)\n```"
				var match = ReFind( referenceRegex, text, 0, true ); 

				expect(len(match.len)).toBe(3);

				expect(match.len[1]).toBe(18);
				expect(match.len[2]).toBe(0);
				expect(match.len[3]).toBe(10);

				expect(match.pos[1]).toBe(2);
				expect(match.pos[2]).toBe(0);
				expect(match.pos[3]).toBe(6);

				expect(isNull(match.match[2])).toBe(true);;
			});
		});
	}

	function beforeAll() {
		application name="LDEV-3690" regex={"type":"perl"}; // again sets to default value(perl) for regex engine
	}
}