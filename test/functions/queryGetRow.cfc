component extends = "org.lucee.cfml.test.LuceeTestCase"	{
	function run( testResults , testBox ) {

		myQry = QueryNew("id,name","Integer,VarChar",[[1,'Lucee'],[2,'Lucee1']]);
		
		describe( title = "Test suite for queryGetRow", body = function() {
			it( title = 'Test case for queryGetRow in function',body = function( currentSpec ) {
				
				assertEquals('2',structfind(queryGetRow(myQry,2),"id"));
				assertEquals('Lucee1',structfind(queryGetRow(myQry,2),"name"));
				assertEquals('1',structfind(queryGetRow(myQry,1),"id"));
				assertEquals('Lucee',structfind(queryGetRow(myQry,1),"name"));
			});
			it( title = 'Test case for queryGetRow in member-function',body = function( currentSpec ) {
				
				assertEquals('2',structfind(myQry.GetRow(2),"id"));
				assertEquals('Lucee1',structfind(myQry.GetRow(2),"name"));
				assertEquals('1',structfind(myQry.GetRow(1),"id"));
				assertEquals('Lucee',structfind(myQry.GetRow(1),"name"));
			});
		})
	}
}