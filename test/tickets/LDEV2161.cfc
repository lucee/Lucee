component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( "test suite for LDEV-2161", function() {
			it(title = "REFind() with sub expressions returns incorrect match elements", body = function( currentSpec ) {
				var result = reFind("oo([0-9]+)", "foo42", 1, true);
				var res = refind("(a+c)+","acacachgacacerrracaccaacc",1,true);

				//Checking with reporter's input
				expect(result.len[1]).toBe('4');
				expect(result.len[2]).toBe('2');
				expect(result.match[1]).toBe('oo42');
				expect(result.match[2]).toBe('42');
				expect(result.pos[1]).toBe('2');
				expect(result.pos[2]).toBe('4');
				//Checking one more possibility
				expect(res.len[1]).toBe('6');
				expect(res.len[2]).toBe('2');
				expect(res.match[1]).toBe('acacac');
				expect(res.match[2]).toBe('ac');
				expect(res.pos[1]).toBe('1');
				expect(res.pos[2]).toBe('5');
			});
 		});
	}		
}