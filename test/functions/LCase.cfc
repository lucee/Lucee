component extends="org.lucee.cfml.test.LuceeTestCase"	{
	public void function testUCase(){
		expect( LCase( "A B C" ) ) .toBe( "a b c" );
	}
}