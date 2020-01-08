component extends="org.lucee.cfml.test.LuceeTestCase"{
	// skip closure
	function isNotSupported() {
		variables.s3Details=getCredentials();
		if(!isNull(variables.s3Details.ACCESSKEYID) && !isNull(variables.s3Details.AWSSECRETKEY)) {
			variables.supported = true;
		}
		else
			variables.supported = false;

		return !variables.supported;
	}

	function beforeAll() skip="isNotSupported"{
		if(isNotSupported()) return;
		s3Details = getCredentials();
		mitrahsoftBucketName = "lucee-testsuite-ldev1176";
		base = "s3://#s3Details.ACCESSKEYID#:#s3Details.AWSSECRETKEY#@";
		baseWithBucketName = "s3://#s3Details.ACCESSKEYID#:#s3Details.AWSSECRETKEY#@/#mitrahsoftBucketName#";
		// for skipping rest of the cases, if error occurred.
		hasError = false;
		// for replacing s3 access keys from error msgs
		regEx = "\[[a-zA-Z0-9\:\/\@]+\]";
		path = baseWithBucketName;
		for ( i=1;i<=100;i++){
			path &= "/#i#";
		}
		if( !directoryExists(path) )
			directoryCreate(path);
	}
	
	function afterAll() skip="isNotSupported"{
		if(isNotSupported()) return;
		 if( directoryExists(baseWithBucketName) )
		 	directoryDelete(baseWithBucketName, true);
	}

	public function run( testResults , testBox ) {
		describe( title="Test suite for LDEV-1176 ( checking directoryExists() with large s3 bucket )", body=function() {
			aroundEach( function( spec, suite ){
				if(!hasError)
					arguments.spec.body();
			});

			it(title="Checking time elapsed for directoryExists() with a path, which has 100 levels", skip=isNotSupported(), body=function( currentSpec ) {
				if(isNotSupported()) return;
				uri = createURI("LDEV1176/test.cfm");
				// Dummy request
				local.result = {};
				for(x=1;x<=5;x++){
					local.result["key#x#"] = _InternalRequest(
						template:uri
					);
					expect(local.result["key#x#"].fileContent).toBe("0|0|0|0|0");
					sleep(3000);
				}
			});
		});
	}

	// Private functions
	private struct function getCredentials() {
		var s3 = {};
		if(!isNull(server.system.environment.S3_ACCESS_ID) && !isNull(server.system.environment.S3_SECRET_KEY)) {
			// getting the credentials from the environment variables
			s3.ACCESSKEYID=server.system.environment.S3_ACCESS_ID;
			s3.AWSSECRETKEY=server.system.environment.S3_SECRET_KEY;
		}else if(!isNull(server.system.properties.S3_ACCESS_ID) && !isNull(server.system.properties.S3_SECRET_KEY)) {
			// getting the credentials from the system variables
			s3.ACCESSKEYID=server.system.properties.S3_ACCESS_ID;
			s3.AWSSECRETKEY=server.system.properties.S3_SECRET_KEY;
		}
		return s3;
	}

	private string function createURI(string calledName){
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI & "" & calledName;
	}
}
