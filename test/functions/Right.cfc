component extends="org.lucee.cfml.test.LuceeTestCase" {

	public void function testRight() {

		expect ( right( "abcd", 3 ) ).toBe( 'bcd' );
		expect ( right( "abcd", 5 ) ).toBe( 'abcd' );
		expect ( right( "Peter", -1 ) ).toBe( 'eter' );

		expect ( function() {
			right( "abcd", 0 );
		 }).toThrow( );

	}

}