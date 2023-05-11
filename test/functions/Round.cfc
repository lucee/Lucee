component extends="org.lucee.cfml.test.LuceeTestCase" {

	public void function testRound() {

		var pi = 3.1415926535;
		expect( Round( pi ) ).toBe ( 3 );
		expect( Round( pi, 3 ) ).toBe ( 3.142 );
		
		expect ( function() {
			Round( "pi", 3 );
		 }).toThrow( );

	}

}