component extends="org.lucee.cfml.test.LuceeTestCase" {

	public void function testMillisccond() {
		var ms = 86;
		var d = createDateTime(2015, 02, 04, 11, 22, 33, ms);
		expect (millisecond( d ) ).toBe( 86 );

		d = dateAdd("l", 10, d );
		expect (millisecond( d ) ).toBe( ms + 10 );
	}

}
