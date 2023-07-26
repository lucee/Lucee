component extends="org.lucee.cfml.test.LuceeTestCase" {
	function run( testResults , testBox ) {
		describe( title = "Testcase for xmlNew() function", body = function() {
			it( title = "checking xmlNew() function", body = function( currentSpec ) {
				var XmlDocument = xmlNew();
				XMLDocument.XmlRoot = "element";

				expect( XMLDocument.XmlRoot.xmlName ).toBe("element");
				expect( XMLDocument.keyExists("XmlRoot") ).toBeTrue();
				expect( isXml(XmlDocument) ).toBeTrue();
			});
		});
	}
}