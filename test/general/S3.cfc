component extends="org.lucee.cfml.test.LuceeTestCase" labels="s3" {
	function beforeAll(){
	}

	function afterAll(){
	}

	function run( testResults , testBox ) {
		local.has=structCount(getCredentials());
		describe( "test suite for S3 VFS", function() {
			if(!has) return;
			it(title="test this.s3", body=function() {
				var uri=createURI("s3/default-s3/index.cfm");
				local.res=_InternalRequest(
					template:uri
					,url:getCredentials());
				expect(trim(res.filecontent?:"")).toBe(true);
			});
			it(title="test this.vfs.s3", body=function() {
				var uri=createURI("s3/default-vfs-s3/index.cfm");
				local.res=_InternalRequest(
					template:uri
					,url:getCredentials());
				expect(trim(res.filecontent?:"")).toBe(true);
			});
			it(title="test this.vfs.s3.lucee", body=function() {
				var uri=createURI("s3/mapping-vfs-s3/index.cfm");
				local.res=_InternalRequest(
					template:uri
					,url:getCredentials());
				expect(trim(res.filecontent?:"")).toBe(true);
			});
			it(title="test this.vfs.s3[.lucee]", body=function() {
				var uri=createURI("s3/default-mapping-vfs-s3/index.cfm");
				local.res=_InternalRequest(
					template:uri
					,url:getCredentials());
				expect(trim(res.filecontent?:"")).toBe("truetrue");
			});
			it(title="test access 100 threads at the same time", body=function() {
				var cred=getCredentials();
				var maxAttempts=3;
				for ( var attempt=1; attempt <= maxAttempts; attempt++ ) {
					var dir="s3://#cred.ACCESS_KEY_ID#:#cred.SECRET_KEY#@/#cred.BUCKET_PREFIX#s3-#lcase( hash( CreateGUID() ) )#/";
					var file=dir&"testmultithread.txt";
					var error = {};
					try {
						if(!directoryExists(dir))directoryCreate(dir);
						fileWrite(file, "Susi sorglos foehnte Ihr Haar!");

						var arr=[];
						loop times=100 {
							arrayAppend(arr, file);
						}

						arr.each(
						function(el, ix, arr) localMode=true {
							var res=fileRead(el);
						}, true, 100);
					} catch ( e ){
						error = e;
					} finally {
						try {
							if(directoryExists(dir))directoryDelete(dir, true);
						} catch ( cleanupError ) {
							// cleanup can hit the same connection problem; don't let it mask the result
							if ( !structCount( error ) && !isTransientConnectionError( cleanupError ) ) error = cleanupError;
						}
					}
					if ( !structCount( error ) ) return;
					// only connection-level problems (S3 not responding, dropped TLS handshake) are retried / skipped,
					// anything else is a real failure
					// never hide the real problem: surface type, message, detail and the full cause chain
					if ( !isTransientConnectionError( error ) ) {
						throw ( type=( error.type ?: "S3TestFailure" ), message="S3 100 threads test failed (attempt #attempt#): " & describeError( error, cred.SECRET_KEY ), detail=replace( ( error.detail ?: "" ), cred.SECRET_KEY, "***", "all" ), cause=error );
					}
					if ( attempt < maxAttempts ) sleep( 1000 * attempt );
				}
				skip( "S3 not reachable after #maxAttempts# attempts: " & describeError( error, cred.SECRET_KEY ) );
			});
		});
	}

	// LDEV-6667 connection-level errors from S3 that are not caused by Lucee
	private boolean function isTransientConnectionError( required any e ) {
		var patterns = [ "NoHttpResponseException", "failed to respond", "SSLHandshakeException",
			"Remote host terminated the handshake", "Connection reset", "SocketTimeoutException",
			"Read timed out", "connect timed out", "Unable to execute HTTP request" ];
		var current = arguments.e;
		var depth = 0;
		while ( !isNull( current ) && depth++ < 10 ) {
			var text = ( current.type ?: "" ) & " " & ( current.message ?: "" ) & " " & ( current.detail ?: "" );
			try { text &= " " & current.getClass().getName() & " " & ( current.getMessage() ?: "" ); } catch ( ignore ) {}
			for ( var p in patterns ) {
				if ( findNoCase( p, text ) ) return true;
			}
			try { current = current.getCause(); } catch ( ignore ) { current = current.cause ?: nullValue(); }
		}
		return false;
	}

	// LDEV-6667 render an exception and its cause chain, so the test output shows the real error
	private string function describeError( required any e, string secret="" ) {
		var parts = [];
		var current = arguments.e;
		var depth = 0;
		while ( !isNull( current ) && depth++ < 10 ) {
			var cls = "";
			var msg = "";
			try { cls = current.getClass().getName(); msg = current.getMessage() ?: ""; } catch ( ignore ) {}
			var line = "[" & ( current.type ?: cls ) & "] " & ( current.message ?: msg );
			if ( len( current.detail ?: "" ) ) line &= " (detail: " & current.detail & ")";
			if ( len( cls ) && !findNoCase( cls, line ) ) line &= " {" & cls & "}";
			arrayAppend( parts, line );
			var next = nullValue();
			try { next = current.getCause(); } catch ( ignore ) { next = current.cause ?: nullValue(); }
			current = next ?: nullValue();
		}
		var result = arrayToList( parts, " <- caused by: " );
		// the s3:// path carries the credentials, never leak them into the test output
		return len( arguments.secret ) ? replace( result, arguments.secret, "***", "all" ) : result;
	}

	private struct function getCredentials() {
		return server.getTestService("s3");
	}

	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}

}
