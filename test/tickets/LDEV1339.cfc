component extends="org.lucee.cfml.test.LuceeTestCase" labels="xml" {
	function run( testResults , testBox ) {
		describe( "Test suite for LDEV-1339", function() {
			it(title="Checking XMLTransform(), with xml paramater", body = function( currentSpec ) {
				var uri=createURI("LDEV1339/test.cfm");
				local.result = _InternalRequest(
					template:uri,
					forms:{Scene=1}
				);
				expect(result.filecontent.trim()).toBe("false");
			});

			it(title="Checking XMLTransform(), with xml paramater as empty string", body = function( currentSpec ) {
				var uri=createURI("LDEV1339/test.cfm");
				local.result = _InternalRequest(
					template:uri,
					forms:{Scene=2}
				);
				expect(result.filecontent.trim()).notToBe("false");
			});
		});
	}
//javax.xml.transform.TransformerException: oracle.xml.parser.v2.XMLParseException; lineNumber: 1; columnNumber: 1; Start of [secure] element expected.
//javax.xml.transform.TransformerException: org.xml.sax.SAXParseException;          lineNumber: 1; columnNumber: 1; Vorzeitiges Dateiende.]



	// private Function//
	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}