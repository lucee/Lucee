component extends="org.lucee.cfml.test.LuceeTestCase" {

	public void function testQueryAddColumn() localmode="true" {
		qryResult = queryNew( 'column1,column2', "varchar,varchar",[["col1-1","col2-1"],["col1-2","col2-2"]] );
		colCount = queryAddColumn(qryResult,"column3");
		assertEquals(2,qryResult.recordcount());
		assertEquals(3,qryResult.columnCount());

		qryResult = queryNew( 'column1,column2', "varchar,varchar" );
		colCount = queryAddColumn(qryResult,"column3",["col3"]);
		assertEquals(1,qryResult.recordcount());
		assertEquals(3,qryResult.columnCount());
		assertEquals("col3",qryResult.column3[1]);

		qryResult = queryNew( 'column1,column2', "varchar,varchar" );
		colCount = queryAddColumn(qryResult,"column3","varchar");
		assertEquals(0,qryResult.recordcount());
		assertEquals(3,qryResult.columnCount());

		qryResult = queryNew( 'column1,column2', "varchar,varchar" );
		colCount = queryAddColumn(qryResult,"column3","varchar",["col3-1","col3-2"]);
		assertEquals(2,qryResult.recordcount());
		assertEquals(3,qryResult.columnCount());
		assertEquals("col3-1",qryResult.column3[1]);
	}

	public void function testQueryAddColumnMemberFuction() localmode="true" {
		qryResult = queryNew( 'column1,column2' );
		updatedQry = qryResult.AddColumn("column3");
		assertEquals(true,isQuery(updatedQry));
		assertEquals(3,qryResult.columnCount());
		assertEquals(3,updatedQry.columnCount());
	}
}