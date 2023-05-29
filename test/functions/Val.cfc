component extends="org.lucee.cfml.test.LuceeTestCase" {

	public void function testVal() {

		expect( Val( "1234 Main St." ) ).toBe( 1234 );
		expect( Val( "Main St., 1234" ) ).toBe( 0 );
		expect( Val( "123.456" ) ).toBe( 123.456 );
		expect( Val( "" ) ).toBe( 0 );
		expect( Val( "a" ) ).toBe( 0 );
		expect( Val( "1" ) ).toBe( 1 );
		expect( Val( "one" ) ).toBe( 0 );
		expect( Val( "123T456" ) ).toBe( 123 );
	}

}
