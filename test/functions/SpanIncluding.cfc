component extends="org.lucee.cfml.test.LuceeTestCase" {
	public function run( testResults, testBox ) {
		describe( title="Testcase for spanIncluding() function", body=function() {
			it(title="Checking the spanIncluding() function", body=function( currentSpec ) {
				expect(trim(spanIncluding("Plant green! save earth!", "Plant green!"))).toBe("Plant green!");
				expect(trim(spanIncluding("AabByyysss", "AabBz"))).toBe("AabB");
				expect(trim(spanIncluding("AabByyysss", "S"))).toBeEmpty();
				expect(trim(spanIncluding("AabByyysss", ""))).toBeEmpty();
			
			});
			it(title="Checking the string.spanIncluding() member function", body=function( currentSpec ) {
				expect(trim("Plant green! save earth!".spanIncluding("Plant green!"))).toBe("Plant green!");
				expect(trim("AabByyysss".spanIncluding("AabBz"))).toBe("AabB");
				expect(trim("AabByyysss".spanIncluding("H"))).toBeEmpty();
				expect(trim("AabByyysss".spanIncluding(""))).toBeEmpty();
			});
		});
	}
}