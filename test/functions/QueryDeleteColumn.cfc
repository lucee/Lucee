 component extends="org.lucee.cfml.test.LuceeTestCase" {

	public void function testQueryDeleteColumn() localmode="true" {
		qryResult = queryNew( 'column1,column2,column3', "varchar,varchar,varchar",[["col1-1","col2-1","col3-1"],["col1-2","col2-2","col3-2"]] );
		res = queryDeleteColumn(qryResult,"column2");

		assertEquals(true,isArray(res));
		assertEquals("col2-1",res[1]);
		assertEquals(2,qryResult.recordcount());
		assertEquals(2,qryResult.getColumnCount());
		assertEquals(false,queryColumnExists(qryResult,"column2"));
	}

	public void function testQueryDeleteColumnMemberFuction() localmode="true" {
		qryResult = queryNew( 'column1,column2,column3', "varchar,varchar,varchar",[["col1-1","col2-1","col3-1"],["col1-2","col2-2","col3-2"]] );
		updatedqry = qryResult.DeleteColumn("column2");

		assertEquals(true,isQuery(updatedqry));
		assertEquals(2,qryResult.recordcount());
		assertEquals(2,qryResult.getColumnCount());
		assertEquals(false,queryColumnExists(qryResult,"column2"));
		assertEquals(2,updatedqry.recordcount());
		assertEquals(2,updatedqry.getColumnCount());
		assertEquals(false,queryColumnExists(updatedqry,"column2"));
	}
}