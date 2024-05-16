component extends = "org.lucee.cfml.test.LuceeTestCase"	{

	function run( testResults , testBox ) {

		myQry = QueryNew("id,name","Integer,VarChar",[[1,'Lucee'],[2,'Lucee1']]);
		describe( title = "Test suite for querySetRow", body = function() {

			it( title = 'Test case for querySetRow with array',body = function( currentSpec ) {
				var q=query(a:[1,2,3],b:['a','b','c'],c:['m','n','o']);
				querySetRow(query:q,row:2,data:[0,0,0,0]);
				assertEquals('query("a":[1,0,3],"b":["a",0,"c"],"c":["m",0,"o"])',serialize(q));
			});
			it( title = 'Test case for querySetRow with struct',body = function( currentSpec ) {
				var q=query(a:[1,2,3],b:['a','b','c'],c:['m','n','o']);
				querySetRow(query:q,row:3,data:{a:"xxx",c:"xxx"});
				assertEquals('query("a":[1,2,"xxx"],"b":["a","b","c"],"c":["m","n","xxx"])',serialize(q));
			});
		})
		
	}
}