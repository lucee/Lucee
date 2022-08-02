component extends="org.lucee.cfml.test.LuceeTestCase" labels="s3"{
	// skip closure
	function isNotSupported() {
		variables.s3Details=getCredentials();
		if (structIsEmpty(s3Details)) return true;
		if (!isNull(variables.s3Details.ACCESS_KEY_ID) && !isNull(variables.s3Details.SECRET_KEY)) {
			variables.supported = true;
		} else {
			variables.supported = false;
		}
		return !variables.supported;
	}

	function beforeAll() skip="isNotSupported"{
		if(isNotSupported()) return;
		var s3Details = getCredentials();
		variables.bucketName = lcase("lucee-ldev1176-#hash(CreateGUID())#");
		variables.base = "s3://#s3Details.ACCESS_KEY_ID#:#s3Details.SECRET_KEY#@";
		variables.baseWithBucketName = "s3://#s3Details.ACCESS_KEY_ID#:#s3Details.SECRET_KEY#@/#variables.bucketName#";
		// for skipping rest of the cases, if error occurred.
		variables.hasError = false;
		// for replacing s3 access keys from error msgs
		//regEx = "\[[a-zA-Z0-9\:\/\@]+\]";
		var path = variables.baseWithBucketName;
		for ( var i = 1 ; i <= 100 ; i++ ){
			path &= "/#i#";
		}
		if( !directoryExists( path ) )
			directoryCreate( path );
	}
	
	function afterAll() skip="isNotSupported"{
		if (isNotSupported()) return;
		if( directoryExists( baseWithBucketName ) )
			directoryDelete( baseWithBucketName, true );
	}

	public function run( testResults , testBox ) {
		describe( title="Test suite for LDEV-1176 ( checking directoryExists() with large s3 bucket )", body=function() {
			aroundEach( function( spec, suite ){
				if(!hasError)
					arguments.spec.body();
			});

			it(title="Checking time elapsed for directoryExists() with a long path, which has 100 levels", skip=isNotSupported(), body=function( currentSpec ) {
				if(isNotSupported()) return;
				local.uri = createURI("LDEV1176/test.cfm");
				// Dummy request
				local.result = {};
				for(x=1;x<=5;x++){
					local.result["key#x#"] = _InternalRequest(
						template:uri,
						url: { bucketName: bucketname}
					);
					expect(local.result["key#x#"].fileContent).toBe("0|0|0|0|0");
					sleep(3000);
				}
			});
		});
	}

	// Private functions
	private struct function getCredentials() {
		return server.getTestService("s3");
	}

	private string function createURI(string calledName){
		var baseURI = "/test/#listLast(getDirectoryFromPath( getCurrenttemplatepath() ), "\/ ")#/";
		return baseURI & "" & arguments.calledName;
	}
}
