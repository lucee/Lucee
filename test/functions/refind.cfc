component extends = "org.lucee.cfml.test.LuceeTestCase"	{

	function run( testResults , testBox ) {
		describe( title = "Test suite for refind function", body = function() {

			it( title = 'no match, request only first match',body = function( currentSpec ) {
				var res=reFind("(f)(oo)", "bar", 1, true);
				assertEquals(3,structCount(res));
				assertEquals(0,res.len[1]);
				assertEquals("",res.match[1]);
				assertEquals(0,res.pos[1]);
			});

			it( title = 'no match, request all match',body = function( currentSpec ) {
				var res=reFind("(f)(oo)", "bar", 1, true,"all");
				assertEquals(1,arrayLen(res));
				res=res[1];
				assertEquals(3,structCount(res));
				assertEquals(0,res.len[1]);
				assertEquals("",res.match[1]);
				assertEquals(0,res.pos[1]);
			});

		})
		
	}
}