component extends="org.lucee.cfml.test.LuceeTestCase"{

	// skip closure
	function isNotSupported() {
		variables.s3Details=getCredentials();
		return structIsEmpty(s3Details);
	}

	function beforeAll() skip="isNotSupported"{
		if(isNotSupported()) return;
		s3Details = getCredentials();
		variables.mitrahsoftBucketName = "LDEV1774";
		variables.baseWithBucketName = "s3://#s3Details.ACCESS_KEY_ID#:#s3Details.SECRET_KEY#@/#variables.mitrahsoftBucketName#";	
		variables.URI = createURI("LDEV1774");
	}

	public function run( testResults , testBox ) {
		describe( title="Test suite for LDEV-1774", body=function() {
			it(title="Creating a new s3 bucket", skip=isNotSupported(), body=function( currentSpec ) {
				if(isNotSupported()) return;
				if( directoryExists(baseWithBucketName))
					directoryDelete(baseWithBucketName, true);
				directoryCreate(baseWithBucketName);
			});

			it(title="checking file MIME type in s3, for CSS file", skip=isNotSupported(), body=function( currentSpec ){
				cffile (action="copy", source="#variables.URI#\test.css", destination=baseWithBucketName & "/test.css");
				cfhttp(method="get", url="http://s3.amazonaws.com/#variables.mitrahsoftBucketName#/test.css", result="local.result") {
				}
				expect(local.result.mimetype).toBe("text/css");
			});

			it(title="checking file MIME type in s3, for PDF file", skip=isNotSupported(), body=function( currentSpec ){
				cffile (action="copy", source="#variables.URI#\test.pdf", destination=baseWithBucketName & "/test.pdf");
				cfhttp(method="get", url="http://s3.amazonaws.com/#variables.mitrahsoftBucketName#/test.pdf", result="local.result") {
				}
				expect(local.result.mimetype).toBe("application/pdf");
			});

			it(title="checking file MIME type in s3, for Image file", skip=isNotSupported(), body=function( currentSpec ){
				cffile (action="copy", source="#variables.URI#\test.jpg", destination=baseWithBucketName & "/test.jpg");
				cfhttp(method="get", url="http://s3.amazonaws.com/#variables.mitrahsoftBucketName#/test.jpg", result="local.result") {
				}
				expect(local.result.mimetype).toBe("image/jpeg");
			});
		});
	}


	// Private functions
	private struct function getCredentials() {
		return server.getTestService("s3");
	}

	private string function createURI(string calledName){
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI & "" & calledName;
	}
}

