component extends="org.lucee.cfml.test.LuceeTestCase"	{

	public void function testUCFirst(){

		expect( UCFirst( "susi" ) ).toBeWithCase( "Susi" );
		
		// doAll=boolean
		expect( UCFirst("susi sorglos", true ) ).toBeWithCase( "Susi Sorglos" );
		expect( UCFirst("susi sorglos", false ) ).toBeWithCase( "Susi sorglos" );

		// doAll=boolean, doLowerIfAllUppercase=boolean
		expect( UCFirst("SORGLOS", true, false ) ).toBeWithCase( "SORGLOS" );
		expect( UCFirst("SORGLOS", true, true ) ).toBeWithCase( "Sorglos" );

		// member functions
		expect( "susi".ucFirst() ).toBeWithCase( "Susi" );

		// doAll=boolean
		expect( "susi sorglos".ucFirst( true ) ).toBeWithCase( "Susi Sorglos" );
		expect( "susi sorglos".ucFirst( false ) ).toBeWithCase( "Susi sorglos" );

		// doAll=boolean, doLowerIfAllUppercase=boolean
		expect( "SORGLOS".ucFirst( true, false ) ).toBeWithCase( "SORGLOS" );
		expect( "SORGLOS".ucFirst (true, true ) ).toBeWithCase( "Sorglos" );

	}
} 

