component extends="org.lucee.cfml.test.LuceeTestCase" {
	function run(){
		describe( title="Test cases for LDEV-339(arrayContainsNoCase problem)", body=function(){
			beforeEach(function( currentSpec ){
				x = ["user_manage"];
			});
			it(title="arrayContains", body=function(){
				// This shouldn't find any result, as it is a substring.
				expect(arrayContains(x, "user")).toBe("0");
				// This shouldn't find any result, as it has trailing spaces.
				expect(arrayContains(x, "user_manage ")).toBe("0");
				// This shouldn't find any result, as it has case difference
				expect(arrayContains(x, "user_managE")).toBe("0");
				// This should find the result on first position
				expect(arrayContains(x, "user_manage")).toBe("1");
			});

			it(title="arrayContains with substring match", body=function(){
				// This should find the result, as it has a substring with same case.
				expect(arrayContains(x, "user", true)).toBe("1");
				// This shouldn't find any result, as it has a substring with different case.
				expect(arrayContains(x, "useR", true)).toBe("0");
				// This shouldn't find any result, as it has trailing spaces with same case.
				expect(arrayContains(x, "user_manage ", true)).toBe("0");
				// This shouldn't find any result, as it has trailing spaces with different case.
				expect(arrayContains(x, "user_managE ", true)).toBe("0");
				// This shouldn't find any result, as it has case difference
				expect(arrayContains(x, "user_managE", true)).toBe("0");
				// This should find the result on first position
				expect(arrayContains(x, "user_manage", true)).toBe("1");
			});

			it(title="arrayContainsNoCase", body=function(){
				// This shouldn't find any result, as it is a substring.
				expect(arrayContainsNoCase(x, "user")).toBe("0");
				// This shouldn't find any result, as it has trailing spaces.
				expect(arrayContainsNoCase(x, "user_manage ")).toBe("0");
				// This should find the result, even though it has different case
				expect(arrayContainsNoCase(x, "user_managE")).toBe("1");
				// This should find the result on first position
				expect(arrayContainsNoCase(x, "user_manage")).toBe("1");
			});

			it(title="arrayContainsNoCase with substring match", body=function(){
				// This should find the result, as it has a substring with same case.
				expect(arrayContainsNoCase(x, "user", true)).toBe("1");
				// This should find the result, as it has a substring with different case.
				expect(arrayContainsNoCase(x, "useR", true)).toBe("1");
				// This shouldn't find any result, as it has trailing spaces with same case.
				expect(arrayContainsNoCase(x, "user_manage ", true)).toBe("0");
				// This shouldn't find any result, as it has trailing spaces with different case.
				expect(arrayContainsNoCase(x, "user_managE ", true)).toBe("0");
				// This should find the result, even though it has different case
				expect(arrayContainsNoCase(x, "user_managE", true)).toBe("1");
				// This should find the result on first position
				expect(arrayContainsNoCase(x, "user_manage", true)).toBe("1");
			});

			it(title="arrayFind", body=function(){
				// This shouldn't find any result, as it is a substring.
				expect(arrayFind(x, "user")).toBe("0");
				// This shouldn't find any result, as it has trailing spaces.
				expect(arrayFind(x, "user_manage ")).toBe("0");
				// This shouldn't find the result, even though it has different case
				expect(arrayFind(x, "user_managE")).toBe("0");
				// This should find the result on first position
				expect(arrayFind(x, "user_manage")).toBe("1");
			});
			it(title="arrayFindNoCase", body=function(){
				// This shouldn't find any result, as it is a substring.
				expect(arrayFindNoCase(x, "user")).toBe("0");
				// This shouldn't find any result, as it has trailing spaces.
				expect(arrayFindNoCase(x, "user_manage ")).toBe("0");
				// This should find the result, even though it has different case
				expect(arrayFindNoCase(x, "user_managE")).toBe("1");
				// This should find the result on first position
				expect(arrayFindNoCase(x, "user_manage")).toBe("1");
			});
		});
	}
}