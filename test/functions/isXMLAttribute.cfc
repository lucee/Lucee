component extends="org.lucee.cfml.test.LuceeTestCase" labels="isXmlAttribute" {

	function run( testResults , testBox ) {
		describe( title = "Testcase for isXMLAttribute function", body = function() {
			it( title = "checking isXmlAttribute() function", body = function( currentSpec ) {
				var path='<note test="aaa"><from body="bbb">Bob</from></note>';
				var res = XmlSearch(path, '//@test');
				expect(isXMLAttribute(res[1])).toBe(true);
			});
		});
	}
}