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
		mitrahsoftBucketName = "testcasesLDEV0359";
		base = "s3://#s3Details.ACCESSKEYID#:#s3Details.AWSSECRETKEY#@";
		baseWithBucketName = "s3://#s3Details.ACCESSKEYID#:#s3Details.AWSSECRETKEY#@/#mitrahsoftBucketName#";
		// for skipping rest of the cases, if error occurred.
		hasError = false;
		// for replacing s3 access keys from error msgs
		regEx = "\[[a-zA-Z0-9\:\/\@]+\]";
	}

	function afterAll() skip="isNotSupported"{
		if(isNotSupported()) return;
		if( directoryExists(baseWithBucketName) )
			directoryDelete(baseWithBucketName, true);
	}

	public function run( testResults , testBox ) {
		describe( title="Test suite for LDEV-359 ( checking s3 file operations )", body=function() {
			aroundEach( function( spec, suite ){
				if(!hasError)
					arguments.spec.body();
			});

			it(title="Creating a new s3 bucket", skip=isNotSupported(), body=function( currentSpec ) {
				if(isNotSupported()) return;
				hasErrorInternal = false;
				try{
					if( directoryExists(baseWithBucketName))
						directoryDelete(baseWithBucketName, true);
					directoryCreate(baseWithBucketName);
				} catch(any e){
					hasError = true;
					hasErrorInternal = true;
				}
				expect(hasErrorInternal).toBeFalse();
			});

			it(title="Creating a new file without extension", skip=isNotSupported(), body=function( currentSpec ) {
				hasErrorInternal = false;
				try{
					if(!fileExists(baseWithBucketName & "/a"))
						fileWrite(baseWithBucketName & "/a", "");
				} catch(any e){
					hasError = true;
					hasErrorInternal = true;
				}
				expect(hasErrorInternal).toBeTrue();
			});

			it(title="Creating a new file by the newly created file(blank extension) as a folder", skip=isNotSupported(), body=function( currentSpec ) {
				hasErrorInternal = false;
				try{
					if(!fileExists(baseWithBucketName & "/a/foo.txt"))
						fileWrite(baseWithBucketName & "/a/foo.txt", "hello there");
				} catch(any e){
					hasError = true;
					hasErrorInternal = true;
					ErrorMsg = reReplaceNoCase(e.Message, regEx, "", "ALL");
				}
				expect(hasErrorInternal).toBeTrue();
				expect(ErrorMsg).toBe("directory doesn't exist");
			});

			it(title="Trying to access the file(no extension) as a directory", skip=isNotSupported(), body=function( currentSpec ) {
				hasErrorInternal = false;
				ErrorMsg = "";
				try{
					myContent = directoryList(baseWithBucketName & "/a", true);
				} catch(any e){
					hasError = true;
					hasErrorInternal = true;
					ErrorMsg = reReplaceNoCase(e.Message, regEx, "", "ALL");
				}
				expect(hasErrorInternal).toBeTrue();
				expect(ErrorMsg).toBe("directory doesn't exist");
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
}
