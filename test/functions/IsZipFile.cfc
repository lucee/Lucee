component extends="org.lucee.cfml.test.LuceeTestCase" {

	function beforeAll() {
		variables.uri = createURI("isZipFolder_/");
		variables.zipFile = uri&"sample.zip";
		variables.file = zipFile&"/testSample.cfm";
		if(!directoryExists(uri)) {
			directoryCreate(uri);
		}
		// zip
		zip action="zip" file=zipFile {
			zipparam entryPath = "/testSample.cfm" content="I love lucee";
		}
	}

	function run( testResults , testBox ) {
		describe( "Testcase for isZipFile()", function() {
			it(title="Checking the isZipFile() function", body=function( currentSpec ) {
				expect(isZipFile(zipFile)).toBe(true);
				expect(isZipFile(file)).toBe(false);
			});
		});
	}

	private string function createURI(string calledName) {
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrentTemplatePath()), "\/")#/";
		return baseURI&""&calledName;
	}

	function afterAll() {
		if(directoryExists(uri)) {
			directoryDelete(uri, true);
		}
	}
}