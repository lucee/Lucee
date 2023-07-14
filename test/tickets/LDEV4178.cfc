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
		bucketName = server.getTestService("s3").bucket_prefix & lcase("4178-#lcase(hash(CreateGUID()))#");
		return "s3://#s3Details.ACCESS_KEY_ID#:#s3Details.SECRET_KEY#@/#bucketName#";
	}

	private numeric function checkS3Version(){
		var s3Version = extensionList().filter(function(row){
			return (row.name contains "s3");
		}).version;
		return listFirst( s3Version, "." ) ;
	};

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
				if ( checkS3Version() neq 0 ) {
					var info = StoreGetMetadata( bucket ); // only works with v2 due to https://luceeserver.atlassian.net/browse/LDEV-4202
					expect( info ).toHaveKey( "region" );
					expect( info.region ).toBe( arguments.storelocation );
				}
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

			it(title="Creating a new s3 bucket, valid region name [eu-west-1]", skip=isNotSupported(), body=function( currentSpec ) {
				createBucket( "eu-west-1" ); // fails
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

