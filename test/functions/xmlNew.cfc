component extends="org.lucee.cfml.test.LuceeTestCaseParallel" {
	function run( testResults , testBox ) {
		describe( title = "Testcase for xmlNew() function", body = function() {
			parallel( title = "checking xmlNew() function",threadCount=5, repetitition=2, body = function( currentSpec ) {
				var XmlDocument = xmlNew();
				XMLDocument.XmlRoot = "element";

				expect( XMLDocument.XmlRoot.xmlName ).toBe("element");
				expect( XMLDocument.keyExists("XmlRoot") ).toBeTrue();
				expect( isXml(XmlDocument) ).toBeTrue();
			});
		});
	}
}