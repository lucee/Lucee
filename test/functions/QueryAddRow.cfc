component extends="org.lucee.cfml.test.LuceeTestCase"	{

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
}