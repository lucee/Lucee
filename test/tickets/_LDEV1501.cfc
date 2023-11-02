component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( "Test suite for LDEV-1501", function() {
			it( title='Checking Webservice call with login creditinal', body=function( currentSpec ) {
				var wb = createObject("webservice","http://mail.welikeit.net/Services/svcDomainAdmin.asmx?wsdl");
				var result = wb.GetDomainInfo(AuthUserName="Administrator", AuthPassword="noPassword", domainName="welikeit.net");
				expect(result.getmessage()).toBE("Failed to log in.");
			});

			it( title='Sending Soap Request Via HTTP', body=function( currentSpec ) {
				saveContent variable="local.soapBody" {
					writeOutPut(
					'<?xml version="1.0" encoding="utf-8"?>
					<soap:Envelope xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
					<soap:Body>
					<GetUserQuotas xmlns="http://tempuri.org/">
					<AuthUserName>Administrator</AuthUserName>
					<AuthPassword>asdfsadfsdf</AuthPassword>
					<DomainName>welikeit.net</DomainName>
					</GetUserQuotas>
					</soap:Body>
					</soap:Envelope>');
				}
				cfhttp(method="POST", url="http://mail.ilikeit.net/Services/svcUserAdmin.asmx", result="local.result") {
					cfhttpparam(name="SOAPAction", type="header", value="http://tempuri.org/GetUserQuotas" );
					cfhttpparam(type="xml", value="#trim( local.soapBody )#" );
				}
				expect(isXML(local.result.fileContent)).toBE(true);
			});
		});
	}
}