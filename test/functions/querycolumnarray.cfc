component extends="org.lucee.cfml.test.LuceeTestCase"	{
	function run( testResults , testBox ) {

		myQry=QueryNew("id,name","Integer,VarChar",[[1,'Lucee'],[2,'Lucee1']]);
		describe( title="Test suite for queryColumnArray", body=function() {
			it( title='Test case for queryColumnArray in function',body=function( currentSpec ) {
				assertEquals('TRUE',isArray(queryColumnArray(myQry)));
				assertEquals('id',queryColumnArray(myQry)[1]);
				assertEquals('name',queryColumnArray(myQry)[2]);
			});

			it( title='Test case for queryColumnArray in member-function',body=function( currentSpec ) {
				assertEquals('TRUE',isArray(myQry.ColumnArray()));
				assertEquals('id',myQry.ColumnArray()[1]);
				assertEquals('name',myQry.ColumnArray()[2]);
			});
		})

	}
}