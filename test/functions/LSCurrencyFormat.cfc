component extends="org.lucee.cfml.test.LuceeTestCase"{

	function beforeAll(){
		setLocale("en_us");
	}

	function afterAll(){
		setLocale("en_us");
	}

	function run( testResults , testBox ) {
		describe( title="Test suite for LSCurrencyFormat()", body=function() {
			it(title="checking LSCurrencyFormat() function", body = function( currentSpec ) {
				orgLocale=getLocale();

				dt=CreateDateTime(2004,1,2,4,5,6);
				euro=chr(8364);
				<!--- 
				English (Australian) --->
				setLocale("English (Australian)");
				assertEquals("$100,000.00", "#LSCurrencyFormat(100000)#");
				assertEquals("100,000.00", "#LSCurrencyFormat(100000,"none")#");
				assertEquals("$100,000.00", "#LSCurrencyFormat(100000,"local")#");
				assertEquals("AUD100,000.00", "#replace(LSCurrencyFormat(100000,"international","English (Australian)"),' ','')#");

				<!--- 
				German (Standard)) --->
				setLocale("German (Standard)");
				assertEquals("100.000,00 #euro#", "#LSCurrencyFormat(100000,"local")#");
				assertEquals("EUR100.000,00", "#replace(LSCurrencyFormat(100000,"international"),' ','')#");
				assertEquals("100.000,00", "#LSCurrencyFormat(100000,"none")#");

				<!--- 
				German (Swiss) --->
				setLocale("German (Swiss)");
				if(getJavaVersion()>=9) {
					if(getJavaVersion()>=11) assertEquals("CHF 100’000.00", "#LSCurrencyFormat(100000,"local")#");
					else assertEquals("CHF 100'000.00", "#LSCurrencyFormat(100000,"local")#");
					assertEquals("CHF 1.00", "#LSCurrencyFormat(1)#");
					assertEquals("CHF 1.20", "#LSCurrencyFormat(1.2)#");
					assertEquals("CHF 1.20", "#LSCurrencyFormat(1.2,"local")#");
				}
				else {	
					assertEquals("SFr. 100'000.00", "#LSCurrencyFormat(100000,"local")#");
					assertEquals("SFr. 1.00", "#LSCurrencyFormat(1)#");
					assertEquals("SFr. 1.20", "#LSCurrencyFormat(1.2)#");
					assertEquals("SFr. 1.20", "#LSCurrencyFormat(1.2,"local")#");
				}

				if(getJavaVersion()>=11) {
					assertEquals("CHF100’000.00", "#replace(LSCurrencyFormat(100000,"international"),' ','')#");
					assertEquals("100’000.00", "#LSCurrencyFormat(100000,"none")#");
				}
 				else {
					assertEquals("CHF100'000.00", "#replace(LSCurrencyFormat(100000,"international"),' ','')#");
					assertEquals("100'000.00", "#LSCurrencyFormat(100000,"none")#");
				}

				assertEquals("CHF1.20", "#replace(LSCurrencyFormat(1.2,"international")," ","")#");
				assertEquals("1.20", "#LSCurrencyFormat(1.2,"none")#");

				try{
					assertEquals("x", "#LSCurrencyFormat(1.2,"susi")#");
					fail("must throw:Parameter 2 of function LSCurrencyFormat has an invalid value of ""susi"". "".""."".""."".""."".""."".""."".");
				} catch ( any e ){}


				<!--- 
				German (Standard) --->
				setLocale("German (Standard)");

				assertEquals("1,00 #euro#", "#LSCurrencyFormat(1)#");
				assertEquals("1,20 #euro#", "#LSCurrencyFormat(1.2)#");

				assertEquals("1,20 #euro#", "#LSCurrencyFormat(1.2,"local")#");
				assertEquals("EUR1,20", "#replace(LSCurrencyFormat(1.2,"international")," ","")#");
				assertEquals("1,20", "#LSCurrencyFormat(1.2,"none")#");


				setLocale("German (Swiss)");
				value="250.000";
				if(getJavaVersion()>=9) {
					assertEquals("CHF 250.00", "#LSCurrencyFormat(value,"local")#");
					assertEquals("CHF 250.00", "#LSCurrencyFormat(value)#");
				}
				else {	
					assertEquals("SFR. 250.00", "#LSCurrencyFormat(value,"local")#");
					assertEquals("SFR. 250.00", "#LSCurrencyFormat(value)#");
				}
				assertEquals("250", "#LSParseNumber(value)#");
				assertEquals("CHF250.00", "#replace(LSCurrencyFormat(value,'international'),' ','','all')#");
				assertEquals("250.00", "#LSCurrencyFormat(value,'none')#");


				setLocale("Portuguese (Brazilian)");
				value=250000;
				if(getJavaVersion()>=11 || getJavaVersion()<9) {
					assertEquals("R$ 250.000,00", "#LSCurrencyFormat(value)#");
				}
				else {	
					assertEquals("R$250.000,00", "#LSCurrencyFormat(value)#");
				}
				assertEquals("250000", "#LSParseNumber(value)#");

				value=250.000;
				if(getJavaVersion()>=11 || getJavaVersion()<9) {
					assertEquals("R$ 250,00", "#LSCurrencyFormat(value)#");
				}
				else {	
					assertEquals("R$250,00", "#LSCurrencyFormat(value)#");
				}
				assertEquals("250", "#LSParseNumber(value)#");

				value="250000";
				if(getJavaVersion()>=11 || getJavaVersion()<9) {
					assertEquals("R$ 250.000,00", "#LSCurrencyFormat(value)#");
				}
				else {	
					assertEquals("R$250.000,00", "#LSCurrencyFormat(value)#");
				}
				assertEquals("250000", "#LSParseNumber(value)#");

				value="250,000";
				assertEquals("250", "#LSParseNumber(value)#");


				value="250.000";
				if(getJavaVersion()>=11 || getJavaVersion()<9) {
					assertEquals("R$ 250,00", "#LSCurrencyFormat(value,"local","Portuguese (Brazilian)")#");
					assertEquals("R$ 250,00", "#LSCurrencyFormat(value)#");
				}
				else {	
					assertEquals("R$250,00", "#LSCurrencyFormat(value,"local","Portuguese (Brazilian)")#");
					assertEquals("R$250,00", "#LSCurrencyFormat(value)#");
				}
				assertEquals("250000", "#LSParseNumber(value)#");
				assertEquals("BRL250,00", "#replace(LSCurrencyFormat(value,'international'),' ','','all')#");
				assertEquals("250,00", "#LSCurrencyFormat(value,'none')#");

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

