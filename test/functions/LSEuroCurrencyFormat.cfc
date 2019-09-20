
component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( title="Test suite for LSEuroCurrencyFormat()", body=function() {
			it(title="checking LSEuroCurrencyFormat() function", body = function( currentSpec ) {
				<!--- begin old test code --->
				orgLocale=getLocale();
				setLocale("German (Swiss)");
				dt=CreateDateTime(2004,1,2,4,5,6);
				euro=chr(8364);

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
		});
	}

	private function getJavaVersion() {
        var raw=server.java.version;
        var arr=listToArray(raw,'.');
        if(arr[1]==1) // version 1-9
            return arr[2];
        return arr[1];
    }
}
