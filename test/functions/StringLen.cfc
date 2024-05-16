component extends="org.lucee.cfml.test.LuceeTestCase" {

	public void function testStringLen() {

		expect ( stringLen( "abcd" ) ).toBe( 4 );
		expect ( stringLen( 123 ) ).toBe( 3 );  // number is cast to string

		// member functions
		expect ( "abcd".len() ).toBe( 4 );
		expect ( "123".len() ).toBe( 3 );  // number is cast to string

	}

}