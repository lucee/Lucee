component extends="org.lucee.cfml.test.LuceeTestCase" labels="s3" {
	// skip closure
	function isNotSupported() {
		variables.s3Details=getCredentials();
		return structIsEmpty(s3Details);
	}

	function beforeAll() skip="isNotSupported"{
		if(isNotSupported()) return;
	}

	private string function getTestBucketUrl() localmode=true {
		s3Details = getCredentials();
		bucketName = lcase("lucee-ldev4178-#lcase(hash(CreateGUID()))#");
		return "s3://#s3Details.ACCESS_KEY_ID#:#s3Details.SECRET_KEY#@/#bucketName#";
	}

	private function createBucket( required string storelocation, boolean invalid=false ){
		var bucket = getTestBucketUrl();
		try {
			expect( directoryExists( bucket ) ).toBeFalse();
			if ( arguments.invalid ) {
				expect( function(){
					directory action="create" directory="#bucket#" storelocation="#arguments.storelocation#";
				}).toThrow();
			} else {
				directory action="create" directory="#bucket#" storelocation="#arguments.storelocation#";
				expect( directoryExists( bucket ) ).toBeTrue();
				var info = StoreGetMetadata( bucket );
				expect( info ).toHaveKey( "region" );
				expect( info.region ).toBe( arguments.storelocation );
			}
		} finally {
			if ( directoryExists( bucket ) )
				directoryDelete( bucket );
		}
	}

	public function run( testResults , testBox ) {
		describe( title="Test suite for LDEV-1489 ( checking s3 file operations )", body=function() {
			it(title="Creating a new s3 bucket, valid region name [us-east-1]", skip=isNotSupported(), body=function( currentSpec ) {
				createBucket( "us-east-1" );
			});

			it(title="Creating a new s3 bucket, invalid region name [down-under]", skip=isNotSupported(), body=function( currentSpec ){
				createBucket( "down-under", true );
			});
		});
	}

		// Private functions
	private struct function getCredentials() {
		return server.getTestService("s3");
	}

}

