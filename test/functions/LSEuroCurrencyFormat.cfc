
component extends="org.lucee.cfml.test.LuceeTestCase" labels="currency" {
	function run( testResults , testBox ) {
		describe( title="Test suite for LSEuroCurrencyFormat()", body=function() {
			beforeEach( function(){
				variables.startingTZ=getTimeZone();
				setTimeZone("UTC");
            });
			afterEach( function(){
                setTimeZone(variables.startingTZ?:"UTC");
            });
			it(title="checking LSEuroCurrencyFormat() function", body = function( currentSpec ) {
				<!--- begin old test code --->
				var orgLocale=getLocale();
				setLocale("German (Swiss)");
				var dt=CreateDateTime(2004,1,2,4,5,6);
				var euro=chr(8364);

				if(getJavaVersion()>=9) {
					assertEquals("CHF 1.00", "#LSEuroCurrencyFormat(1)#");
					assertEquals("CHF 1.20", "#LSEuroCurrencyFormat(1.2)#");
					assertEquals("CHF 1.20", "#LSEuroCurrencyFormat(1.2,"local")#");
				}
				else {	
					assertEquals("SFr. 1.00", "#LSEuroCurrencyFormat(1)#");
					assertEquals("SFr. 1.20", "#LSEuroCurrencyFormat(1.2)#");
					assertEquals("SFr. 1.20", "#LSEuroCurrencyFormat(1.2,"local")#");
				}


				assertEquals("CHF1.20", "#replace(LSEuroCurrencyFormat(1.2,"international","German (Swiss)")," ","")#");
				assertEquals("1.20", "#LSEuroCurrencyFormat(1.2,"none")#");


				try{
					assertEquals("x", "#LSEuroCurrencyFormat(1.2,"susi")#");
					fail("must throw:Parameter 2 of function LSCurrencyFormat has an invalid value of ""susi"". "".""."".""."".");
				} catch ( any e ){}


				setLocale("German (Standard)");
				assertEquals("1,00 #euro#", "#LSEuroCurrencyFormat(1)#");
				assertEquals("1,20 #euro#", "#LSEuroCurrencyFormat(1.2)#");

				assertEquals("1,20 #euro#", "#LSEuroCurrencyFormat(1.2,"local")#");
				assertEquals("EUR1,20", "#replace(LSEuroCurrencyFormat(1.2,"international")," ","")#");
				assertEquals("1,20", "#LSEuroCurrencyFormat(1.2,"none")#");
				setLocale(orgLocale);
			});

			// LDEV-5486: an empty locale falls back to the current locale
			it(title="LSEuroCurrencyFormat() with an empty locale uses the current locale", body = function( currentSpec ) {
				var orgLocale = getLocale();
				try {
					setLocale( "English (United States)" );
					expect( LSEuroCurrencyFormat( 1.2, "international", "" ) ).toBe( LSEuroCurrencyFormat( 1.2, "international" ) );
					expect( LSEuroCurrencyFormat( 1.2, "local", "" ) ).toBe( LSEuroCurrencyFormat( 1.2, "local" ) );
					expect( LSEuroCurrencyFormat( 1.2, "international", "" ) ).notToInclude( "XXX" );
					expect( LSCurrencyFormat( 1.2, "international", "" ) ).toBe( LSCurrencyFormat( 1.2, "international" ) );
				}
				finally {
					setLocale( orgLocale );
				}
			});
		});
	}

	private function getJavaVersion() {
        var raw=server.java.version;
        var arr=listToArray(raw,'.');
        if(arr[1]==1) // version 1-9
            return arr[2];
        return listFirst( arr[1], "-" ); // return 25 from java 25-ea
    }
}
