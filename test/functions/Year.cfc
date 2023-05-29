component extends="org.lucee.cfml.test.LuceeTestCase" {
	
	public void function testYear() {
		var y = 1984;
		var d = createDateTime( y, 02, 04, 11, 22, 33, 86 );
		expect( year( d ) ).toBe( y );

		d = dateAdd( "yyyy", 10, d );
		expect( year( d ) ).toBe( y + 10 );
	}

}
