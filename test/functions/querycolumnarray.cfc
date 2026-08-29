component extends="org.lucee.cfml.test.LuceeTestCase"	{
	function run( testResults , testBox ) {

		variables.myQry=QueryNew("id,name","Integer,VarChar",[[1,'Lucee'],[2,'Lucee1']]);
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
			// For LDEV-5748 – queryColumnNames as an alias for queryColumnArray
			it( title='queryColumnNames() works as alias for queryColumnArray()',body=function( currentSpec ) {
				assertEquals('TRUE',isArray(queryColumnNames(myQry)));
				assertEquals('id',queryColumnNames(myQry)[1]);
				assertEquals('name',queryColumnNames(myQry)[2]);
			});
		})

	}
}