<cfcomponent extends="org.lucee.cfml.test.LuceeTestCase" labels="s3">
	<cfscript>
		// skip closure
		function isNotSupported() {
			variables.s3Details=getCredentials();
			return structIsEmpty(s3Details);
		}

		function beforeAll() skip="isNotSupported"{
			if ( isNotSupported() ) return;
			variables.bucketName = lcase("lucee-ldev1129-#CreateGUID()#");
			variables.testFolder = createURI( variables.bucketName );

			if (not directoryExists(testFolder) ){
				Directorycreate(testFolder);
				Directorycreate("#testFolder#/test");
				Directorycreate("#testFolder#/test2");
			}
			
			var s3Details = getCredentials();
			
			variables.base = "s3://#s3Details.ACCESS_KEY_ID#:#s3Details.SECRET_KEY#@";
			variables.baseWithBucketName = "s3://#s3Details.ACCESS_KEY_ID#:#s3Details.SECRET_KEY#@/#bucketName#";
			// for skipping rest of the cases, if error occurred.
			variables.hasError = false;
			// for replacing s3 access keys from error msgs
			// variables.regEx = "\[[a-zA-Z0-9\:\/\@]+\]";
		}

		function afterAll() skip="isNotSupported"{
			if (isNotSupported()) return;
			if (directoryExists(baseWithBucketName) )
			 	directoryDelete(baseWithBucketName, true);
			if (directoryExists(testFolder) )
			 	directoryDelete(testFolder, true);
		}

		public function run( testResults , testBox ) {
			describe( title="Test suite for LDEV-1129 ( checking s3 file operations )", body=function() {
				aroundEach( function( spec, suite ){
					if(!hasError)
						arguments.spec.body();
				});

				it(title="Creating a new s3 bucket", skip=isNotSupported(), body=function( currentSpec ) {
					if ( isNotSupported() ) return;
					if( directoryExists(baseWithBucketName) )
						directoryDelete(baseWithBucketName, true);
					directoryCreate(baseWithBucketName);
				});

				// we accept this because S3 accept this, so if ACF does not, that is a bug/limitation in ACF.
				it(title="Creating a new file without extension", skip=isNotSupported(), body=function( currentSpec ) {
					if ( isNotSupported() ) return;
					if (!fileExists(baseWithBucketName & "/test.txt"))
						fileWrite( baseWithBucketName & "/test.txt", "Sample s3 text" );
				});
			
				it(title="Trying to access the file via url of the file name", skip=isNotSupported(), body=function( currentSpec ) {
					if ( isNotSupported() ) return;
					var callingViaURL = cffilewithURL();
					expect( callingViaURL ).toBe('true');
				});

				it(title="Trying to access the file via bucket path of the file name", skip=isNotSupported(), body=function( currentSpec ) {
					if ( isNotSupported() ) return;
					var accessBucket = directlyAccessBucket();
					expect( accessBucket ).toBe('true');
				});
			});
		}

		// Private functions
		private struct function getCredentials() {
			return server.getTestService("s3");
		}

		private string function createURI(string calledName){
			var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
			return baseURI & "" & calledName;
		}
	</cfscript>
	<cffunction name="cffilewithURL" returntype="Any" access="private">
		<cfscript>
			var uri = createURI("#variables.bucketName#/test");
			file action="copy" source="https://s3.amazonaws.com/#variables.bucketName#/test.txt" destination="#uri#";
			if (FileExists("#uri#/test.txt")){
				FileDelete("#uri#/test.txt")
				return true;
			} else {
				return false;
			}
		</cfscript>	
	</cffunction>
	
	<cffunction name="directlyAccessbucket" returntype="Any" access="private">
		<cfset var uri = createURI("#variables.bucketName#/test2")>
		<cftry>
			<cffile action="copy" source="#baseWithBucketName#/test.txt" destination="#uri#">
			<cfcatch type="any">
				<cfreturn cfcatch.message>
			</cfcatch>
		</cftry>
		<cfscript>	
			if (FileExists("#uri#/test.txt")){
				FileDelete("#uri#/test.txt")
				return true;
			} else {
				return false;
			}
		</cfscript>	
	</cffunction>
</cfcomponent>

