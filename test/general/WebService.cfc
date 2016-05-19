component extends="org.lucee.cfml.test.LuceeTestCase"	{
	
	public void function testWSDLSoapWebservice(){
		var WSDL="http://www.webservicex.net/globalweather.asmx?WSDL";
		var ws=createObject("webservice",WSDL);
		var xml=xmlParse(ws.getCitiesByCountry("Switzerland")).NewDataSet.Table[1];
		data.country=xml.Country.XMLTEXT;
		data.city=xml.City.XMLTEXT;
		assertEquals('Switzerland',data.country);
	}


} 



