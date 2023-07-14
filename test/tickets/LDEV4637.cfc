component extends="org.lucee.cfml.test.LuceeTestCase" {
	function beforeAll() {
		variables.uri = createURI("LDEV4637");
		if(isNotSupported()) return;
	}

	function isNotSupported() {
		variables.s3Details=server.getTestService("s3");
		return structIsEmpty(s3Details);
	}
	private string function getTestBucketUrl() localmode=true {
		s3Details = server.getTestService("s3");
		bucketName = s3Details.bucket_prefix & lcase("4637-#lcase(hash(CreateGUID()))#");
		return "s3://#s3Details.ACCESS_KEY_ID#:#s3Details.SECRET_KEY#@/#bucketName#";
	}

	private function tests3( params	) {
		var result = _InternalRequest(
			template : "#uri#\LDEV4637.cfm",
			url: arguments.params,
			form: {
				resource: "s3:///"
			}
		);
		return result.fileContent.trim();
	}

	function run( testResults , testBox ) {
		describe( title = "Testcase for this.s3.defaultLocation", body = function() {

			fileResult = "false";
			dirResult = "true";

			host = "s3.amazonaws.com";
			hostRegion ="s3.eu-west-1.amazonaws.com";
			region = "eu-west-1";

			it( title="directoryExists host s3.eu-west-1.amazonaws.com", body=function( currentSpec ) {
				var result = testS3( {
					host: hostRegion,
					type: "dir"
				});
				expect( result ).toBe(dirResult);
			});

			it( title="fileExists host s3.amazon.com", body=function( currentSpec ) {
				var result = testS3( {
					host: host,
					type: "file"
				});
				expect( result ).toBe( fileResult );
			});


			it( title="directoryExists s3.eu-west-1.amazonaws.com", body=function( currentSpec ) {
				var result = testS3( {
					host: hostRegion,
					type: "dir"
				});
				expect( result ).toBe("true");
			});

			it( title="fileExists host s3.eu-west-1.amazonaws.com", body=function( currentSpec ) {
				var result = testS3( {
					host: hostRegion,
					type: "file"
				});
				expect( result ).toBe( fileResult );
			});


			it( title="fileExists region eu-west-1", body=function( currentSpec ) {
				var result = testS3( {
					region: region,
					type: "file"
				});
				expect( result ).toBe( fileResult );
			});

			it( title="directoryExists region eu-west-1", body=function( currentSpec ) {
				var result = testS3( {
					region: region,
					type: "dir"
				});
				expect( result ).toBe( dirResult );
			});


			it( title="fileExists region eu-west-1, host s3.eu-west-1.amazonaws.com", body=function( currentSpec ) {
				var result = testS3( {
					region: region,
					host: hostRegion,
					type: "file"
				});
				expect( result ).toBe( fileResult );
			});

			it( title="directoryExists region eu-west-1, host s3.eu-west-1.amazonaws.com", body=function( currentSpec ) {
				var result = testS3( {
					region: region,
					host: hostRegion,
					type: "dir"
				});
				expect( result ).toBe( dirResult );
			});


			it( title="fileExists region eu-west-1, host s3.amazon.com", body=function( currentSpec ) {
				var result = testS3( {
					region: region,
					host: host,
					type: "file"
				});
				expect( result ).toBe( fileResult );
			});

			it( title="directoryExists region eu-west-1, host s3.amazon.com", body=function( currentSpec ) {
				var result = testS3( {
					region: region,
					host: host,
					type: "dir"
				});
				expect( result ).toBe( dirResult );
			});

		});
	}

	private string function createURI(string calledName) {
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}
