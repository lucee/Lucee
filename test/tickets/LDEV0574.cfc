component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( "Test suite for LDEV-1574", function() {
			it( title='Checking dollarformat function round off', body=function( currentSpec ) {
				assertEquals("$1.99",DollarFormat("01.985"));
				assertEquals("$11.99",DollarFormat("11.985"));
				assertEquals("$999.99",DollarFormat("999.985"));
				assertEquals("$675.99",DollarFormat("675.985"));
				assertEquals("$555.55",DollarFormat("555.555"));
				assertEquals("$987.56",DollarFormat("987.555"));
			});

			it( title='Checking numberformat function round off', body=function( currentSpec ) {
				assertEquals("$1.99",numberformat("01.985", '9,999.99'));
				assertEquals("$11.99",numberformat("11.985", '9,999.99'));
				assertEquals("999.99",numberformat("999.985", '9,999.99'));
				assertEquals("675.99",numberformat("675.985", '9,999.99'));
				assertEquals("555.55",numberformat("555.555", '9,999.99'));
				assertEquals("987.55",numberformat("987.555", '9,999.99'));
			});

			it( title='Checking lsCurrencyFormat function round off', body=function( currentSpec ) {
				assertEquals("$1.99",lsCurrencyFormat("01.985"));
				assertEquals("$11.99",lsCurrencyFormat("11.985"));
				assertEquals("$999.99",lsCurrencyFormat("999.985"));
				assertEquals("$675.99",lsCurrencyFormat("675.985"));
				assertEquals("$555.555",lsCurrencyFormat("555.555"));
				assertEquals("$987.55",lsCurrencyFormat("987.555"));
			});
		});
	}
}