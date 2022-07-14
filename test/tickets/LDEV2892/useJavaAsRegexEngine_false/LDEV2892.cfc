component extends="org.lucee.cfml.test.LuceeTestCase"{

	function run( testResults , testBox ) {
		describe( "test case for useJavaAsRegexEngine_false", function() {
			it(title = "reFind examples with regex", body = function( currentSpec ) {
				expect(reFind("[0-9]+","test 123!")).toBe('6');
				expect(reFind("[0-9]+","test 1234",7,true).pos[1]).toBe('7');
			});

			it(title = "reFindnocase examples with regex", body = function( currentSpec ) {
				expect(reFindnocase("[a-z]+","1212 aAbBcCdD",6,"true")['len'][1]).toBe('8');
				expect(reFindnocase("[a-z]+","1212 aAbBcCdD",6,"true")['match'][1]).toBe('aAbBcCdD');
				expect(reFindnocase("[a-z]+","1212 aAbBcCdD",6,"false")).toBe('6');
			});

			it(title = "rematch examples with regex", body = function( currentSpec ) {
				str = "count 1234 or one,two,THREE,four.."
				expect(arraylen(rematch("[a-z]+",str))).toBe('5');
				expect(rematch("[a-z]+",str)[1]).toBe('count');
				str = "count 1234 or one,two,three,four.."
				expect(rematch("[a-z]+",str)[2]).toBe('or');
				expect(rematch("[0-9]+",str)[1]).toBe('1234');
				
			});

			it(title = "rematchNoCase examples with regex", body = function( currentSpec ) {
				str = "count 1234 or one,two,THREE,four.."
				expect(arraylen(rematchNoCase("[a-z]+",str))).toBe('6');
				expect(rematchNoCase("[a-z]+",str)[5]).toBe('three');
				str = "count 1234 or one,two,three,four.."
				expect(rematchNoCase("[a-z]+",str)[2]).toBe('or');
				expect(rematchNoCase("[0-9]+",str)[1]).toBe('1234');
			});

			it(title = "reReplace examples with regex", body = function( currentSpec ) {
				var whereList = 'AND "ID" = ?';
				expect(reReplace( whereList, "and\s|or\s", "", "one" )).toBe('AND "ID" = ?');
			});

			it(title = "reReplaceNoCase examples with regex", body = function( currentSpec ) {
				var whereList = 'AND "ID" = ?';
				expect(reReplaceNoCase( whereList, "and\s|or\s", "", "one" )).toBe('"ID" = ?');
			});
		});
	}
}