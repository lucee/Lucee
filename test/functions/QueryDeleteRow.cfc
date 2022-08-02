 component extends="org.lucee.cfml.test.LuceeTestCase" {

	public void function testQueryDeleteRow() localmode="true" {
		qryResult = queryNew( 'column1,column2', "varchar,varchar",[["col1-1","col2-1"],["col1-2","col2-2"],["col1-3","col2-3"]] );
		res = queryDeleteRow(qryResult,2);

		assertEquals(true,res);
		assertEquals(2,qryResult.recordcount());
		assertEquals(2,qryResult.getColumnCount());
		assertEquals("col1-3",qryResult.column1[2]);

		res = queryDeleteRow(qryResult);

		assertEquals(true,res);
		assertEquals(1,qryResult.recordcount());
		assertEquals(2,qryResult.getColumnCount());
		assertEquals("col1-1",qryResult.column1[1]);

	}

	public void function testQueryDeleteRowMemberFuction() localmode="true" {
		qryResult = queryNew( 'column1,column2', "varchar,varchar",[["col1-1","col2-1"],["col1-2","col2-2"],["col1-3","col2-3"]] );
		updatedqry = qryResult.DeleteRow(2);

		assertEquals(true,isQuery(updatedqry));
		assertEquals(2,qryResult.recordcount());
		assertEquals(2,qryResult.getColumnCount());
		assertEquals("col1-3",qryResult.column1[2]);
		assertEquals(2,updatedqry.recordcount());
		assertEquals(2,updatedqry.getColumnCount());
		assertEquals("col1-3",updatedqry.column1[2]);

	}
}