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
		bucketName = server.getTestService("s3").bucket_prefix & lcase("4635-#lcase(hash(CreateGUID()))#");
		return "s3://#s3Details.ACCESS_KEY_ID#:#s3Details.SECRET_KEY#@/#bucketName#";
	}

	private numeric function checkS3Version(){
		var s3Version = extensionList().filter(function(row){
			return (row.name contains "s3");
		}).version;
		return listFirst( s3Version, "." ) ;
	};

	private function copyToBucket( required string bucket, required string storelocation, required string renameLocation, boolean invalid=false ){
		try {
			var renameBucket = "";
			var srcDir = getTempDirectory() & createUniqueID() & "/";
			expect( directoryExists( arguments.bucket ) ).toBeFalse(); // exists creates a false positive

			directoryCreate( srcDir );
			fileWrite(srcDir & "region.txt", storelocation );

			if ( arguments.invalid ) {
				expect( function(){
					directory action="copy" directory="#srcDir#" destination="#arguments.bucket#" storelocation="#arguments.storelocation#";
				}).toThrow();
			} else {
				// try coping local dir to a new s3 bucket with a region
				try {
					directory action="copy" directory="#srcDir#" destination="#arguments.bucket#" storelocation="#arguments.storelocation#";
				} catch (e){
					throw REReplaceNoCase(e.stacktrace,"[***]", "all");
				}
				expect( directoryExists( arguments.bucket ) ).toBeTrue();
				if ( checkS3Version() neq 0 ) {
					var info = StoreGetMetadata( arguments.bucket ); // only works with v2 due to https://luceeserver.atlassian.net/browse/LDEV-4202
					expect( info ).toHaveKey( "region" );
					expect( info.region ).toBe( arguments.storelocation );
				}

				// now try rename to a bucket in a different region 
				// fails between regions https://luceeserver.atlassian.net/browse/LDEV-4639
				if ( len( renameLocation ) ) {
					renameBucket = getTestBucketUrl();
					try {
						directory action="rename" directory="#arguments.bucket#" newDirectory="#renameBucket#" storelocation="#arguments.renameLocation#";
					} catch (e){
						throw REReplaceNoCase(e.stacktrace,"[***]", "all");
					}
					expect( directoryExists( renameBucket ) ).toBeTrue();
					if ( checkS3Version() neq 0 ) {
						var info = StoreGetMetadata( renameBucket ); // only works with v2 due to https://luceeserver.atlassian.net/browse/LDEV-4202
						expect( info ).toHaveKey( "region" );
						expect( info.region ).toBe( arguments.renameLocation );
					}
				}

			}
		} finally {
			if ( directoryExists( srcDir ) )
				directoryDelete( srcDir, true );
			if ( directoryExists( bucket ) )
				directoryDelete( bucket, true );
			if ( !isEmpty( renameBucket ) and directoryExists( renameBucket ) )
				directoryDelete( renameBucket, true );
		}
	}

	public function run( testResults , testBox ) {
		describe( title="Test suite for LDEV-4635 ( checking s3 copy directory operations )", body=function() {
			it(title="Copying dir to a new s3 bucket, valid region name [us-east-1]", skip=isNotSupported(), body=function( currentSpec ) {
				copyToBucket( getTestBucketUrl(), "us-east-1", "us-east-1" );
			});

			it(title="Copying dir to a new s3 bucket, valid region name [eu-west-1]", skip=isNotSupported(), body=function( currentSpec ) {
				copyToBucket( getTestBucketUrl(), "eu-west-1", "eu-west-1" );
			});

			xit(title="Copying dir to a new s3 bucket, valid region name [eu-west-1]", skip=isNotSupported(), body=function( currentSpec ) {
				copyToBucket( getTestBucketUrl(), "eu-west-1", "eu-central-1" ); // fails, can't current copy between regions LDEV-4639
			});

			it(title="Copying dir to a new s3 bucket, invalid region name [down-under]", skip=isNotSupported(), body=function( currentSpec ){
				copyToBucket( getTestBucketUrl(), "down-under", "", true );
			});
		});
	}

		// Private functions
	private struct function getCredentials() {
		return server.getTestService("s3");
	}

}

