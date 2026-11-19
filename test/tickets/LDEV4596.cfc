component extends="org.lucee.cfml.test.LuceeTestCase" labels="list" {

	function run( testResults, testBox ) {
		describe( title = "Testcase for LDEV-4596", body = function() {
			it( title = "Checking listFirst() function", body = function( currentSpec ) {
				var list = 'aa,bb b,cc,ddd,';

				expect(listFirst(list, "")).toBe("a");
				expect(listFirst(list, "", true)).toBe("a");
				expect(listFirst(list, "", false, 4)).toBe("aa,b");
				expect(listFirst(list, ",")).toBe("aa");
				expect(listFirst(list, ",", false, 2)).toBe("aa,bb b");
				expect(listFirst(list)).toBe("aa");
			});

			it( title = "Checking listFirst() member function", body = function( currentSpec ) {
				var list = 'aa,bb b,cc,ddd,';

				expect(list.listFirst("")).toBe("a");
				expect(list.listFirst(",")).toBe("aa");
				expect(list.listFirst(",", false, 2)).toBe("aa,bb b");
			});

			it( title = "empty delimiter is the same as listToArray( list, """" ).first()", body = function( currentSpec ) {
				var list = 'dev,LAS,lucee';
				expect(listFirst(list, "")).toBe(listToArray(list, "").first());
				expect(listLast(list, "")).toBe(listToArray(list, "").last());
			});

			it( title = "Checking listLast() function", body = function( currentSpec ) {
				var list = 'aa,bb b,cc,ddd';

				expect(listLast(list, "")).toBe("d");
				expect(listLast(list, "", true)).toBe("d");
				expect(listLast(list, "", false, 5)).toBe("c,ddd");
				expect(listLast(list, ",")).toBe("ddd");
				expect(listLast(list, ",", false, 2)).toBe("cc,ddd");
				expect(list.listLast("")).toBe("d");
				expect(list.listLast(",")).toBe("ddd");
			});

			it( title = "empty list", body = function( currentSpec ) {
				expect(listFirst("", "")).toBe("");
				expect(listLast("", "")).toBe("");
			});
		});
	}
}
