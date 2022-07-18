component extends="org.lucee.cfml.test.LuceeTestCase" labels="xml" {

	function run( testResults , testBox ) {
		describe( "test suite for LDEV2936", function() {

			it(title = "Using xmlElemNew() function", body = function( currentSpec ) {
				var xml_document = XmlNew();
				var xml_root = xmlElemNew(xml_document,"notes");
			 	var xml_document.XmlRoot = xml_root;
				expect(xml_document.XmlRoot.xmlname).toBe("notes");
			});

			it(title = "Using xml.elemNew() function", body = function( currentSpec ) {
				var xml_document = XmlNew();
				var xml_root = xml_document.elemNew("student");
			 	var xml_document.XmlRoot = xml_root;
				expect(xml_document.XmlRoot.xmlname).toBe('student');
			});
		});
	}
}