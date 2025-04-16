component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults, testBox ){
		describe( "Test case for LDEV-3532", function() {
			it( title="reFind() no matches with regex type java", body=function( currentSpec ){
				cfapplication(regex = {type="java"});
				expect(reFind("(f)(oo)", "bar", 1, false)).toBe(0);
				expect(reFind("(f)(oo)", "bar", 1, false, "ALL")).toBe(0);
			});
			it( title="reFind() no matches with regex type java and returnsubexpressions = true", body=function( currentSpec ) {
				var res=reFind("(f)(oo)", "bar", 1, true);
				expect(res.len[1]).toBe(0);
				expect(res.pos[1]).toBe(0);
				expect(res.match[1]).toBe('');

				var res=reFind("(f)(oo)", "bar", 1, true,"all");
				expect(res[1].len[1]).toBe(0);
				expect(res[1].pos[1]).toBe(0);
				expect(res[1].match[1]).toBe('');
			});
			it( title="reFind() no matches with regex type perl", body=function( currentSpec ){
				try{
					cfapplication(regex = {type="perl"});
					var res = [];
					res[1] = reFind("(f)(oo)", "bar", 1, false);
					res[2] = reFind("(f)(oo)", "bar", 1, false,"all");
				}
				catch(any e){
					res = e.message;
				}
				expect(serializeJSON(res)).toBe("[0,0]");
			});
			it( title="reFind() no matches with regex type perl and returnsubexpressions = true", body=function( currentSpec ){
				var res=reFind("(f)(oo)", "bar", 1, true);
				expect(res.match[1]).toBe('');
				expect(res.len[1]).toBe(0);
				expect(res.pos[1]).toBe(0);

				var res=reFind("(f)(oo)", "bar", 1, true,"all");
				expect(res[1].match[1]).toBe('');
				expect(res[1].len[1]).toBe(0);
				expect(res[1].pos[1]).toBe(0);

			});
		});
	}
}