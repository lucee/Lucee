component extends="org.lucee.cfml.test.LuceeTestCase" {
	public function run( testResults, testBox ) {
		describe( title="Testcase for spanExcluding() function", body=function() {
			it(title="Checking the spanExcluding() function", body=function( currentSpec ) {
				expect(trim(spanExcluding("Plant green! save earth!", "s"))).toBe("Plant green!");
				expect(trim(spanExcluding("Plant green! save earth!", "S"))).toBe("Plant green! save earth!");
				expect(trim(spanExcluding("Plant green! save earth!", ""))).toBe("Plant green! save earth!");
				expect(trim(spanExcluding("AabByyysssccC", "c"))).toBe("AabByyysss");
				expect(trim(spanExcluding("cAabByyysssccC", "c"))).toBeEmpty();
			});
			it(title="Checking the string.spanExcluding() member function", body=function( currentSpec ) {
				expect(trim("Plant green! save earth!".spanExcluding("s"))).toBe("Plant green!");
				expect(trim("Plant green! save earth!".spanExcluding("S"))).toBe("Plant green! save earth!");
				expect(trim("cAabByyysssccC".spanExcluding("c"))).toBeEmpty();
			});
		});
	}
}