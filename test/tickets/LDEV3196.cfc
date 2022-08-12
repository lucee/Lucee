component extends="org.lucee.cfml.test.LuceeTestCase" labels="s3"{

	function isNotSupported() {
		variables.s3Details = getCredentials();
		return structIsEmpty(s3Details);
	}
	
	function beforeAll() {
		if(isNotSupported()) return;

		variables.s3Details = getCredentials();
		variables.path = "#getDirectoryFromPath(getCurrenttemplatepath())#LDEV3196";
		if(!directoryExists(path)) directoryCreate(path)

		variables.bucketName = "lucee-ldev3196-#lcase(hash(CreateGUID()))#";
		variables.base = "s3://#s3Details.ACCESS_KEY_ID#:#s3Details.SECRET_KEY#@";
		variables.baseWithBucketName = "s3://#s3Details.ACCESS_KEY_ID#:#s3Details.SECRET_KEY#@/#bucketName#";

		fileWrite("#path#/testfile.txt","test");
		cfzip( action="zip",file="#path#/ziptest.zip", source="#path#\testfile.txt");

		if( directoryExists(baseWithBucketName)) directoryDelete(baseWithBucketName, true);
		directoryCreate(baseWithBucketName);
	}

	function afterAll() {
		if(isNotSupported()) return;

		if(directoryExists(path)) directoryDelete(path,true);

		if(directoryExists(baseWithBucketName)) directoryDelete(baseWithBucketName, true);
	}

	public function run( testResults , testBox ) {
		describe( title="Testcase for LDEV-3196", body=function() {

			it(title="checking unzip a file from s3 bucket to s3 bucket", skip=isNotSupported() body=function( currentSpec ) {
				filemove("#path#/ziptest.zip",baseWithBucketName);
				cfzip(action="unzip", file="#baseWithBucketName#/ziptest.zip", destination="#baseWithBucketName#\");

				expect(fileExists("#baseWithBucketName#/testfile.txt")).tobeTrue();
			});

		});
	}

	private struct function getCredentials() {
		return server.getTestService("s3");
	}
}