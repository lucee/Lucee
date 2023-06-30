component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults, testBox ) {
		describe( title = "Testcase for LDEV-4596", body = function() {
			it( title = "Checking listFirst() function", body = function( currentSpec ) {
				var list = 'aa,bb b,cc,ddd,';

				expect(listFirst(list, "")).toBe("a");
				expect(listFirst(list, ",")).toBe("aa");
				expect(listFirst(list, ",", false, 2)).toBe("aa,bb b");
			});

			it( title = "Checking listFirst() member function", body = function( currentSpec ) {
				var list = 'aa,bb b,cc,ddd,';

				expect(list.listFirst("")).toBe("a");
				expect(list.listFirst(",")).toBe("aa");
				expect(list.listFirst(",", false, 2)).toBe("aa,bb b");
			});
		});
	}
}