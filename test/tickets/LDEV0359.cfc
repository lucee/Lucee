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
		mitrahsoftBucketName = "lucee-testsuite";
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
				if( directoryExists(baseWithBucketName))
					directoryDelete(baseWithBucketName, true);
				directoryCreate(baseWithBucketName);
			});

			// we accept this because S3 accept this, so if ACF does not, that is a bug/limitation in ACF.
			it(title="Creating a new file without extension", skip=isNotSupported(), body=function( currentSpec ) {
				if(!directoryExists(baseWithBucketName))
					directoryCreate(baseWithBucketName);

				if(!fileExists(baseWithBucketName & "/a"))
					fileWrite(baseWithBucketName & "/a", "");
			});

			// because previous file is empty it is accepted as directory
			it(title="Creating a new file by the newly created file(blank extension) as a folder", skip=isNotSupported(), body=function( currentSpec ) {
				if(!directoryExists(baseWithBucketName))
					directoryCreate(baseWithBucketName);

				if(!fileExists(baseWithBucketName & "/a/foo.txt"))
					fileWrite(baseWithBucketName & "/a/foo.txt", "hello there");
				
			});

			it(title="Trying to access the file(no extension) as a directory", skip=isNotSupported(), body=function( currentSpec ) {
				var children = directoryList(baseWithBucketName & "/a", true,'query');
				expect(1).toBe(children.recordcount);
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
