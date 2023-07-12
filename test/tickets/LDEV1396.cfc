<cfcomponent extends="org.lucee.cfml.test.LuceeTestCase" labels="s3" skip="true">
	<cfscript>
		// skip closure
		function isNotSupported() {
			variables.s3Details=getCredentials();
			return structIsEmpty(s3Details);
		}

		function beforeAll() skip="isNotSupported"{
			if(isNotSupported()) return;
			var s3Details = getCredentials();
			var mitrahsoftBucketName = lcase( s3Details.bucket_prefix & "1396-#lcase(hash(CreateGUID()))#" );
			var base = "s3://#s3Details.ACCESS_KEY_ID#:#s3Details.SECRET_KEY#@";
			variables.baseWithBucketName = "s3://#s3Details.ACCESS_KEY_ID#:#s3Details.SECRET_KEY#@/#mitrahsoftBucketName#";
		}

		function afterAll() skip="isNotSupported"{
			if(isNotSupported()) return;
			 if( directoryExists(baseWithBucketName) )
			 	directoryDelete(baseWithBucketName, true);
		}

		public function run( testResults , testBox ) {
			describe( title="Test suite for LDEV-1396 ( checking s3 file operations )", body=function() {
				it(title="Creating a new s3 bucket", skip=isNotSupported(), body=function( currentSpec ) {
					if(isNotSupported()) return;
					if( directoryExists(baseWithBucketName))
						directoryDelete(baseWithBucketName, true);
					directoryCreate(baseWithBucketName);
				});

				it(title="checking cffile, with attribute storeAcl = 'private' ", skip=isNotSupported(), body=function( currentSpec ){
					cffile (action="write", file=baseWithBucketName & "/teskt.txt", output="Sample s3 text", storeAcl="private");
					var acl = StoreGetACL( baseWithBucketName & "/teskt.txt" );
					removeFullControl(acl);
					expect(arrayisEmpty(acl)).toBe(true);
				});

				it(title="checking cffile, with attribute storeAcl value as aclObject (an array of struct where struct represents an ACL grant)", skip=isNotSupported(), body=function( currentSpec ){
					arr=[{'group':"all",'permission':"read"}];
					cffile (action="write", file=baseWithBucketName & "/test.txt", output="Sample s3 text", storeAcl="#arr#");
					var acl = StoreGetACL( baseWithBucketName & "/test.txt" );
					removeFullControl(acl);
					expect(acl[1].permission).toBe("read");
				});
			});
		}

		private function removeFullControl(acl) {
			var index=0;
			loop array=acl index="local.i" item="local.el" {
				if(el.permission=="FULL_CONTROL")
					local.index=i;
				
			}
			if(index>0)ArrayDeleteAt( acl, index );
		}

		// Private functions
		private struct function getCredentials() {
			return server.getTestService("s3");
		}
	</cfscript>
</cfcomponent>

