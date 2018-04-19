component extends="org.lucee.cfml.test.LuceeTestCase"{
	function beforeAll(){
		variables.uri = createURI("LDEV1814");
	}

	function run( testResults , testBox ) {
		describe( "Test suite for LDEV-1814", function() {
			it( title='Checking getPageContext() and cfhtmlhead tag without Application CFC', body=function( currentSpec ) {
				local.result = _InternalRequest(
				template:"#variables.uri#/test.cfm"
				);
				testContent = HtmlParse(local.result.filecontent.trim()) ;
				expect(arrayLen(testContent.XmlRoot.XmlChildren[2].XmlChildren)).toBe(1);
			});

			it( title='Checking getPageContext() with cfhtmlhead tag by using Application CFC', body=function( currentSpec ) {
				local.result = _InternalRequest(
				template:"#variables.uri#/sample/test1.cfm"
				);
				testContent = HtmlParse(local.result.filecontent.trim()) ;
				expect(arrayLen(testContent.XmlRoot.XmlChildren[2].XmlChildren)).toBe(1);
			});

			it( title='Checking cfsilent tag around the getPageContext() by using Application CFC', body=function( currentSpec ) {
				local.result = _InternalRequest(
				template:"#variables.uri#/sample/test2.cfm"
				);
				testContent = HtmlParse(local.result.filecontent.trim()) ;
				expect(arrayLen(testContent.XmlRoot.XmlChildren[2].XmlChildren)).toBe(1);
			});

			it( title='Checking head tag around the getPageContext() by using Application CFC', body=function( currentSpec ) {
				local.result = _InternalRequest(
				template:"#variables.uri#/sample/test3.cfm"
				);
				testContent = HtmlParse(local.result.filecontent.trim()) ;
				expect(arrayLen(testContent.XmlRoot.XmlChildren[2].XmlChildren)).toBe(1);
			});

		});
	}

	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}