component extends="org.lucee.cfml.test.LuceeTestCase"  labels="pdf"{
	function beforeAll() {
		uri = createURI("LDEV0510");
		if(not fileExists('#uri#/test.pdf')){
			cfdocument(format="PDF" filename="#uri#/test.pdf");
		}
	}

	function isNotSupported( fName ) {
		var results = getFunctionList().keyArray().sort('textnocase');
		if(arrayFindNocase(results, fName)){
			return false;
		}else{
			return true;
		}
	}

	function run( testResults , testBox ) {
		describe( "Test suite for LDEV-510", function() {
			it( title='Checking isDDx()', skip=isNotSupported( "isDDx" ), body=function( currentSpec ) {
				uri=createURI("LDEV0510/index.cfm");
				local.result = _InternalRequest(
					template:uri,
					forms:{Scene=1}
				);
				expect(result.filecontent.trim()).toBe(false);
			});

			it( title='Checking isPdfFile()', skip=isNotSupported( "isPdfFile" ), body=function( currentSpec ) {
				uri=createURI("LDEV0510/index.cfm");
				local.result = _InternalRequest(
					template:uri,
					forms:{Scene=2}
				);
				expect(result.filecontent.trim()).toBe(true);
			});

			it( title='Checking ajaxLink()', skip=isNotSupported( "ajaxLink" ), body=function( currentSpec ) {
				uri=createURI("LDEV0510/index.cfm");
				local.result = _InternalRequest(
					template:uri,
					forms:{Scene=3}
				);
				expect(result.filecontent.trim()).toBe('http://www.google.com');
			});

			it( title='Checking ajaxOnLoad()', skip=isNotSupported( "ajaxOnLoad" ), body=function( currentSpec ) {
				uri=createURI("LDEV0510/index.cfm");
				local.result = _InternalRequest(
					template:uri,
					forms:{Scene=4}
				);
				expect(result.filecontent.trim()).toBe('true');
			});

			it( title='Checking verifyClient()', skip=isNotSupported( "verifyClient" ), body=function( currentSpec ) {
				uri=createURI("LDEV0510/index.cfm");
				local.result = _InternalRequest(
					template:uri,
					forms:{Scene=5}
				);
				expect(result.filecontent.trim()).toBeTypeOf('boolean');
			});

			it( title='Checking dotNetToCFType()', skip=isNotSupported( "dotNetToCFType" ), body=function( currentSpec ) {
				uri=createURI("LDEV0510/index.cfm");
				local.result = _InternalRequest(
					template:uri,
					forms:{Scene=6}
				);
				expect(result.filecontent.trim()).toBe(true);
			});
			
			it( title='Checking getPrinterInfo()', skip=isNotSupported( "getPrinterInfo" ), body=function( currentSpec ) {
				uri=createURI("LDEV0510/index.cfm");
				local.result = _InternalRequest(
					template:uri,
					forms:{Scene=7}
				);
				expect(result.filecontent.trim()).toBe(true);
			});
		});
	}

	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}