component extends="org.lucee.cfml.test.LuceeTestCase"	{

	function run( testResults , testBox ) {

		describe( "Test suite for testWSDLSoapWebservice", function() {
			it( title='Checking webservice call with simple arguments', body=function( currentSpec ) {
			    var WSDL="http://www.webservicex.net/globalweather.asmx?WSDL";
				var ws=createObject("webservice",WSDL);
				var xml=xmlParse(ws.getCitiesByCountry("Switzerland")).NewDataSet.Table[1];
				var data.country=xml.Country.XMLTEXT;
				var data.city=xml.City.XMLTEXT;
				assertEquals('Switzerland', data.country);
			});

			it( title='Checking webservice call with COMPLEX arguments', body=function( currentSpec ) {
				var WSDL = "http://www.webservicex.net/cashflow.asmx?WSDL";
				var ws=createObject("webservice",WSDL);
				local.result1 = ws.CashFlowPresentVlaueDiscrete(['1200', '12548', '180'], ['1600', '12568', '180'], 1 );
				assertEquals(0, local.result1);
			});

			it( title='Checking webservice call without arguments', body=function( currentSpec ) {
				var WSDL = "http://www.webservicex.net/BibleWebservice.asmx?WSDL";
				var ws = createObject("webservice",WSDL);
				var XML = xmlParse(ws.GetBookTitles()).NewDataSet.Table[1];
				local.result = XML.Book.XmlText;
				local.result2 = XML.BookTitle.XmlText
				assertEquals(1, local.result);
				assertEquals('Genesis', local.result2);
			});

			it( title='Checking webservice call with single argument', body=function( currentSpec ) {
				var WSDL = "http://www.webservicex.net/AustralianPostCode.asmx?WSDL";
				var ws = createObject("webservice",WSDL);
				var XML = xmlParse(ws.GetAustralianPostCodeByLocation('Sydney')).NewDataSet.Table[1];
				local.result = XML.Location.XmlText;
				local.result2 = XML.PostCode.XmlText
				assertEquals('North Sydney', local.result);
				assertEquals(' NSW 2055 (Competition Mail)', local.result2);
			});

			it( title='Checking webservice call with multiple arguments', body=function( currentSpec ) {
				var WSDL = "http://www.webservicex.net/ConvertAngle.asmx?WSDL";
				var ws = createObject("webservice",WSDL);
				local.result = ws.ChangeAngleUnit('90', "fullCircle", "radians");
				assertEquals(565.486677646163, local.result);
			});

			it( title='Checking webservice call with local', body=function( currentSpec ) {
				var base="http://#cgi.HTTP_HOST##GetDirectoryFromPath(cgi.SCRIPT_NAME)#/";
				var ws=createObject('webservice',base&'Remote.cfc?wsdl');
				assertEquals(false, isSoaprequest());
				assertEquals(true, ws.addResponse());
			});
		});
	}
}



