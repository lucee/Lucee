<cfcomponent extends="org.lucee.cfml.test.LuceeTestCase" labels="s3">
	<cfscript>
		// skip closure
		function isNotSupported() {
			variables.s3Details=getCredentials();
			if( structIsEmpty(s3Details)) return true;
			if(!isNull(variables.s3Details.ACCESS_KEY_ID) && !isNull(variables.s3Details.SECRET_KEY)) {
				variables.supported = true;
			}
			else
				variables.supported = false;

			return !variables.supported;
		}

		function beforeAll() skip="isNotSupported"{
			uri = createURI("lucee-testsuite-ldev1129");
			if(not directoryExists(uri)){
				Directorycreate(uri);
				Directorycreate("#uri#/test");
				Directorycreate("#uri#/test2");
			}
			if(isNotSupported()) return;
			s3Details = getCredentials();
			mitrahsoftBucketName = "lucee-testsuite-ldev1129";
			base = "s3://#s3Details.ACCESS_KEY_ID#:#s3Details.SECRET_KEY#@";
			baseWithBucketName = "s3://#s3Details.ACCESS_KEY_ID#:#s3Details.SECRET_KEY#@/#mitrahsoftBucketName#";
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
			describe( title="Test suite for LDEV-1129 ( checking s3 file operations )", body=function() {
				aroundEach( function( spec, suite ){
					if(!hasError)
						arguments.spec.body();
				});

				it(title="Creating a new s3 bucket", skip=isNotSupported(), body=function( currentSpec ) {
					if(isNotSupported()) return;
					if( directoryExists(baseWithBucketName))
						directoryDelete(baseWithBucketName, true);
					directoryCreate(baseWithBucketName);
				});

				// we accept this because S3 accept this, so if ACF does not, that is a bug/limitation in ACF.
				it(title="Creating a new file without extension", skip=isNotSupported(), body=function( currentSpec ) {
					if(!fileExists(baseWithBucketName & "/test.txt"))
						fileWrite(baseWithBucketName & "/test.txt", "Sample s3 text");
				});
			
				it(title="Trying to access the file via url of the file name", skip=isNotSupported(), body=function( currentSpec ) {
					var callingViaURL = cffilewithURL();
					expect(callingViaURL).toBe('true');
				});

				it(title="Trying to access the file via bucket path of the file name", skip=isNotSupported(), body=function( currentSpec ) {
					var accessBucket = dircetlyAcessbucket();
					expect(accessBucket).toBe('true');
				});
			});
		}

		// Private functions
		private struct function getCredentials() {
			return server.getTestService("s3");
		}

		private string function createURI(string calledName){
			var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
			return baseURI&""&calledName;
		}
	</cfscript>
	<cffunction name="cffilewithURL" returntype="Any" access="private">
		<cfset uri = createURI("lucee-testsuite-ldev1129/test")>
		<cffile action="copy" source="https://s3.amazonaws.com/lucee-testsuite-ldev1129/test.txt" destination="#uri#">
		<cfif FileExists("#uri#/test.txt")>
			<cfreturn true>
		</cfif>
		<cfreturn false>
	</cffunction>
	
	<cffunction name="dircetlyAcessbucket" returntype="Any" access="private">
		<cfset uri = createURI("lucee-testsuite-ldev1129/test2")>
		<cftry>
			<cffile action="copy" source="#baseWithBucketName#/test.txt" destination="#uri#">
		<cfcatch type="any">
			<cfreturn cfcatch.message>
		</cfcatch>
		</cftry>
		<cfif FileExists("#uri#/test.txt")>
			<cfreturn true>
		</cfif>
		<cfreturn false>
	</cffunction>
</cfcomponent>

