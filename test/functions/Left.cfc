component extends="org.lucee.cfml.test.LuceeTestCase" {

	public void function testLeft() {

		expect ( left( "abcd", 3 ) ).toBe( 'abc' );
		expect ( left( "abcd", 5 ) ).toBe( 'abcd' );
		expect ( left( "Peter", -1 ) ).toBe( 'Pete' );

		expect ( function() {
			left( "abcd", 0 );
		 }).toThrow( );

	}

}
