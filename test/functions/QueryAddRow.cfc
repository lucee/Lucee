component extends="org.lucee.cfml.test.LuceeTestCase" {

	public void function testAddingComplexValues() localmode="true" {
		var qryResult = queryNew( 'column1,column2' );
		qryResult.addRow( { column1 : 'foo', column2 : 'bar' } );
		qryResult.addRow( { column1 : 1, column2 : 2 } );
		qryResult.addRow( { column1 : { brad : 'wood' }, column2 : { luis : 'majano' } } );
		qryResult.addRow( { column1 : [ 1 ], column2 : [ 'a', 'b', 'c' ] } );

		assertEquals(4,qryResult.recordcount);
		assertEquals(2,qryResult.getColumnCount());
		assertEquals(true,isSimpleValue(qryResult.column1[1]));
		assertEquals(true,isNumeric(qryResult.column1[2]));
		assertEquals(true,isStruct(qryResult.column1[3]));
		assertEquals(true,isArray(qryResult.column1[4]));
	}

	public void function testqueryAddRow() localmode="true" {
		qryResult = queryNew( 'column1,column2' );
		row = queryAddRow(qryresult, 3);
		assertEquals(3,row);
		assertEquals(3,qryResult.recordCount());

		qryResult = queryNew( 'column1,column2' );
		row = queryAddRow(qryresult, ["col1","col2"]);
		assertEquals(1,row);
		assertEquals(1,qryResult.recordCount());
		assertEquals("col1",qryResult.column1[1]);
		assertEquals("col2",qryResult.column2[1]);

		qryResult = queryNew( 'column1,column2' );
		row = queryAddRow(qryresult, [["col1","col2"],["col12","col22"]]);
		assertEquals(2,row);
		assertEquals(2,qryResult.recordCount());
		assertEquals("col12",qryResult.column1[2]);
		assertEquals("col22",qryResult.column2[2]);
	}

	public void function testqueryAddRowMemberFuction() localmode="true" {
		qryResult = queryNew( 'column1,column2' );
		updatedqry = qryResult.AddRow(3);
		assertEquals(true,isQuery(updatedqry));
		assertEquals(3,qryResult.recordCount());
		assertEquals(3,updatedqry.recordCount());
	}
}