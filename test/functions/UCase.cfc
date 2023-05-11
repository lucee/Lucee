component extends="org.lucee.cfml.test.LuceeTestCase"	{

	public void function testUCase(){
		expect( UCase( "a b c" ) ).toBe( "A B C" );
	}

}