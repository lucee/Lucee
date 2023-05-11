component extends="org.lucee.cfml.test.LuceeTestCase" {

	public void function testCJustify(){

		expect ( CJustify( "abc", 1 ) ).toBe( "abc" );
		expect ( "abc".cJustify( 1 ) ).toBe( "abc" );

		expect ( "abc".cJustify( 10 ) ).toBe( "   abc    " );
		expect ( CJustify( "abc", 10 ) ).toBe( "   abc    " );

		expect ( CJustify( "abc", 4 ) ).toBe( "abc " );
		expect ( "abc".cJustify( 4 ) ).toBe( "abc " );

		expect ( CJustify( "abc", 5 ) ).toBe( " abc " );
		expect ( "abc".cJustify( 5 ) ).toBe( " abc " );

		expect ( CJustify( "abc", 6 ) ).toBe( " abc  " );
		expect ( "abc".cJustify( 6 ) ).toBe( " abc  " );

		expect( function(){
			Cjustify( "abc" , 0 );
		}).toThrow();
	}

}