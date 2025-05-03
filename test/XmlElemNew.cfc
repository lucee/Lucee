component extends="org.lucee.cfml.test.LuceeTestCase" {
	function run( testResults , testBox ) {
		describe( title = "Testcase for XmlElemNew() function", body = function() {
			it( title = "Checking XmlElemNew() function", body = function( currentSpec ) {
				var xml_document = XmlNew();
				var xmlelem = XmlElemNew(xml_document,"Lucee");
				expect(isxmldoc(xml_document)).toBeTrue();
				expect(IsXmlElem(xmlelem)).toBeTrue();
			});
		});
	}
}