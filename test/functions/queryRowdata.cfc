component extends = "org.lucee.cfml.test.LuceeTestCase"	{

	function run( testResults , testBox ) {

		myQry = QueryNew("id,name","Integer,VarChar",[[1,'Lucee'],[2,'Lucee1']]);
		describe( title = "Test suite for queryRowdata", body = function() {

			it( title = 'Test case for queryRowdata in function',body = function( currentSpec ) {
				assertEquals('2',structfind(queryRowdata(myQry,2),"id"));
				assertEquals('Lucee1',structfind(queryRowdata(myQry,2),"name"));
				assertEquals('1',structfind(queryRowdata(myQry,1),"id"));
				assertEquals('Lucee',structfind(queryRowdata(myQry,1),"name"));
			});

			it( title = 'Test case for queryRowdata in function returnformat array',body = function( currentSpec ) {
				
				assertEquals('[2,"Lucee1"]',serialize(queryRowdata(myQry,2,"array")));
			});

			

			it( title = 'Test case for queryRowdata in member-function',body = function( currentSpec ) {
				assertEquals('2',structfind(myQry.Rowdata(2),"id"));
				assertEquals('Lucee1',structfind(myQry.Rowdata(2),"name"));
				assertEquals('1',structfind(myQry.Rowdata(1),"id"));
				assertEquals('Lucee',structfind(myQry.Rowdata(1),"name"));
			});
		})
		
	}
}