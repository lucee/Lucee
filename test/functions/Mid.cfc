
component extends="org.lucee.cfml.test.LuceeTestCase"	{

	public void function testMid(){
		expect( mid( "abcd", 2, 2 ) ).toBe( "bc" );
		expect( mid( "abcd", 2, 3 ) ).toBe( "bcd" );
		expect( mid( "abcd", 2) ).toBe( "bcd" );

		expect( function(){
			mid( "abcd", 0 );
		} ).toThrow();

		expect( "abcd".mid( 2, 2 ) ).toBe( "bc" );
		expect( "abcd".mid( 2, 3 ) ).toBe( "bcd" );
		expect( "abcd".mid( 2 ) ).toBe( "bcd" );

		expect( function(){
			"abcd".mid(0);
		} ).toThrow();
	}
}