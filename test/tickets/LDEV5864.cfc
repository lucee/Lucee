component extends="org.lucee.cfml.test.LuceeTestCase" labels="maven" {

	function beforeAll() {
		variables.root = getTempDirectory() & "LDEV5864-" & createUUID() & "/";
		variables.repo = root & "repo/";
		variables.cache = root & "cache/";
		variables.downloads = root & "downloads/";
		directoryCreate( repo, true );
		directoryCreate( cache, true );
		directoryCreate( downloads, true );

		var OutputLevel = createObject( "java", "com.sun.net.httpserver.SimpleFileServer$OutputLevel" );
		var SimpleFileServer = createObject( "java", "com.sun.net.httpserver.SimpleFileServer" );
		var addr = createObject( "java", "java.net.InetSocketAddress" ).init( createObject( "java", "java.net.InetAddress" ).getLoopbackAddress(), 0 );
		variables.requestLog = createObject( "java", "java.io.ByteArrayOutputStream" ).init();
		variables.logFilter = SimpleFileServer.createOutputFilter( requestLog, OutputLevel.INFO );
		variables.httpServer = createObject( "java", "com.sun.net.httpserver.HttpServer" ).create( addr, 0 );
		var ctx = httpServer.createContext( "/", SimpleFileServer.createFileHandler( createObject( "java", "java.io.File" ).init( repo ).toPath() ) );
		ctx.getFilters().add( logFilter );
		httpServer.start();
		variables.headerContexts = [];
		variables.baseUrl = "http://127.0.0.1:" & httpServer.getAddress().getPort() & "/";
		variables.HTTPEngine = createObject( "java", "lucee.commons.net.http.HTTPEngine" );
		variables.DownloadVerifier = createObject( "java", "lucee.commons.net.DownloadVerifier" );
	}

	function afterAll() {
		if ( !isNull( variables.httpServer ) ) httpServer.stop( 0 );
		if ( directoryExists( variables.root ) ) directoryDelete( variables.root, true );
	}

	function run( testResults, testBox ) {
		describe( "LDEV-5864: validate the hash of a download", function() {

			beforeEach( function() {
				// the requests of the previous test are logged before we reset the log
				syncRequestLog();
				for ( var path in headerContexts ) httpServer.removeContext( path );
				headerContexts = [];
				directoryDelete( repo, true );
				directoryCreate( repo );
				directoryDelete( downloads, true );
				directoryCreate( downloads );
				requestLog.reset();
				DownloadVerifier.resetWarned();
			});

			describe( "checksum files next to the artifact", function() {

				it( title="a matching .sha1 passes", body=function() {
					var content = "sha1 content";
					serve( "a/a-1.0.jar", content, {}, { "sha1": hex( content, "SHA-1" ) } );
					var target = download( "a/a-1.0.jar" );
					expect( fileRead( target ) ).toBe( content );
					expect( requestsOf( "a-1.0.jar." ) ).toBe( [ "GET /a/a-1.0.jar.sha512", "GET /a/a-1.0.jar.sha256", "GET /a/a-1.0.jar.sha1" ] );
				});

				it( title="a matching .sha256 passes", body=function() {
					var content = "sha256 content";
					serve( "a/a-1.0.jar", content, {}, { "sha256": hex( content, "SHA-256" ) } );
					expect( fileRead( download( "a/a-1.0.jar" ) ) ).toBe( content );
				});

				it( title="a matching .sha512 passes and the weaker checksums are not asked for", body=function() {
					var content = "sha512 content";
					serve( "a/a-1.0.jar", content, {}, { "sha512": hex( content, "SHA-512" ) } );
					expect( fileRead( download( "a/a-1.0.jar" ) ) ).toBe( content );
					expect( requestsOf( "a-1.0.jar." ) ).toBe( [ "GET /a/a-1.0.jar.sha512" ] );
				});

				it( title="a matching .md5 passes", body=function() {
					var content = "md5 content";
					serve( "a/a-1.0.jar", content, {}, { "md5": hex( content, "MD5" ) } );
					expect( fileRead( download( "a/a-1.0.jar" ) ) ).toBe( content );
				});

				it( title="a checksum file with the name of the file behind the hash passes", body=function() {
					var content = "named content";
					serve( "a/a-1.0.jar", content, {}, { "sha1": hex( content, "SHA-1" ) & "  a-1.0.jar" & chr( 10 ) } );
					expect( fileRead( download( "a/a-1.0.jar" ) ) ).toBe( content );
				});

				it( title="a mismatching .sha1 throws and the file is deleted", body=function() {
					serve( "a/a-1.0.jar", "the content", {}, { "sha1": hex( "something else", "SHA-1" ) } );
					var target = downloads & "mismatch.jar";
					var msg = downloadFails( "a/a-1.0.jar", target );
					expect( msg ).toInclude( "SHA1 validation failed" );
					expect( msg ).toInclude( baseUrl & "a/a-1.0.jar" );
					expect( fileExists( target ) ).toBeFalse();
					expect( directoryList( downloads ) ).toBeEmpty();
				});

				it( title="a mismatching .sha256 throws", body=function() {
					serve( "a/a-1.0.jar", "the content", {}, { "sha256": hex( "something else", "SHA-256" ) } );
					expect( downloadFails( "a/a-1.0.jar" ) ).toInclude( "SHA256 validation failed" );
				});

				it( title="a mismatching .sha512 throws", body=function() {
					serve( "a/a-1.0.jar", "the content", {}, { "sha512": hex( "something else", "SHA-512" ) } );
					expect( downloadFails( "a/a-1.0.jar" ) ).toInclude( "SHA512 validation failed" );
				});

				it( title="a mismatching .md5 throws", body=function() {
					serve( "a/a-1.0.jar", "the content", {}, { "md5": hex( "something else", "MD5" ) } );
					expect( downloadFails( "a/a-1.0.jar" ) ).toInclude( "MD5 validation failed" );
				});

				it( title="the strongest checksum wins, a wrong weaker checksum is ignored", body=function() {
					var content = "strongest";
					serve( "a/a-1.0.jar", content, {}, { "sha512": hex( content, "SHA-512" ), "sha1": hex( "something else", "SHA-1" ), "md5": hex( "something else", "MD5" ) } );
					expect( fileRead( download( "a/a-1.0.jar" ) ) ).toBe( content );
				});

				it( title="the strongest checksum wins, a mismatch is not covered by a matching weaker checksum", body=function() {
					var content = "strongest";
					serve( "a/a-1.0.jar", content, {}, { "sha512": hex( "something else", "SHA-512" ), "sha1": hex( content, "SHA-1" ), "md5": hex( content, "MD5" ) } );
					expect( downloadFails( "a/a-1.0.jar" ) ).toInclude( "SHA512 validation failed" );
				});

				it( title="a checksum file that is an HTML page delivered with a 200 is not a checksum", body=function() {
					var content = "html content";
					serve( "a/a-1.0.jar", content, {}, { "sha512": "<html><body>Not Found</body></html>", "sha256": "<html><body>Not Found</body></html>", "sha1": hex( content, "SHA-1" ) } );
					expect( fileRead( download( "a/a-1.0.jar" ) ) ).toBe( content );
					expect( requestsOf( "a-1.0.jar." ) ).toBe( [ "GET /a/a-1.0.jar.sha512", "GET /a/a-1.0.jar.sha256", "GET /a/a-1.0.jar.sha1" ] );
				});

				it( title="upper case hex in the checksum file passes", body=function() {
					var content = "upper case content";
					serve( "a/a-1.0.jar", content, {}, { "sha1": uCase( hex( content, "SHA-1" ) ) } );
					expect( fileRead( download( "a/a-1.0.jar" ) ) ).toBe( content );
				});
			});

			describe( "hash in the response headers", function() {

				it( title="a matching X-Checksum-SHA1 passes without asking for checksum files", body=function() {
					var content = "header sha1";
					serve( "a/a-1.0.jar", content, { "X-Checksum-SHA1": hex( content, "SHA-1" ) } );
					expect( fileRead( download( "a/a-1.0.jar" ) ) ).toBe( content );
					expect( requestsOf( "a-1.0.jar." ) ).toBeEmpty();
				});

				it( title="a mismatching X-Checksum-SHA1 throws and the file is deleted", body=function() {
					serve( "a/a-1.0.jar", "the content", { "X-Checksum-SHA1": hex( "something else", "SHA-1" ) } );
					var target = downloads & "header-mismatch.jar";
					expect( downloadFails( "a/a-1.0.jar", target ) ).toInclude( "SHA1 validation failed" );
					expect( fileExists( target ) ).toBeFalse();
				});

				it( title="matching X-Checksum-SHA256 and X-Checksum-SHA512 pass", body=function() {
					var content = "header sha2";
					serve( "a/a-1.0.jar", content, { "X-Checksum-SHA256": hex( content, "SHA-256" ) } );
					serve( "b/b-1.0.jar", content, { "X-Checksum-SHA512": hex( content, "SHA-512" ) } );
					expect( fileRead( download( "a/a-1.0.jar" ) ) ).toBe( content );
					expect( fileRead( download( "b/b-1.0.jar" ) ) ).toBe( content );
				});

				it( title="the strongest header wins, whatever the order of the headers is", body=function() {
					var content = "strongest header";
					serve( "a/a-1.0.jar", content, { "X-Checksum-MD5": hex( "something else", "MD5" ), "X-Checksum-SHA1": hex( "something else", "SHA-1" ), "X-Checksum-SHA256": hex( content, "SHA-256" ) } );
					serve( "b/b-1.0.jar", content, { "X-Checksum-SHA1": hex( content, "SHA-1" ), "X-Checksum-MD5": hex( content, "MD5" ), "X-Checksum-SHA256": hex( "something else", "SHA-256" ) } );
					for ( var i = 1; i <= 5; i++ ) {
						expect( fileRead( download( "a/a-1.0.jar" ) ) ).toBe( content );
						expect( downloadFails( "b/b-1.0.jar" ) ).toInclude( "SHA256 validation failed" );
					}
				});

				it( title="the header names are not case sensitive", body=function() {
					var content = "lower case";
					serve( "a/a-1.0.jar", content, { "x-checksum-sha1": hex( content, "SHA-1" ) } );
					serve( "b/b-1.0.jar", content, { "X-CHECKSUM-SHA1": hex( "something else", "SHA-1" ) } );
					expect( fileRead( download( "a/a-1.0.jar" ) ) ).toBe( content );
					expect( downloadFails( "b/b-1.0.jar" ) ).toInclude( "SHA1 validation failed" );
				});

				it( title="a base64 Content-MD5 passes or throws", body=function() {
					var content = "base64 md5";
					serve( "a/a-1.0.jar", content, { "Content-MD5": base64Of( content, "MD5" ) } );
					serve( "b/b-1.0.jar", content, { "Content-MD5": base64Of( "something else", "MD5" ) } );
					expect( fileRead( download( "a/a-1.0.jar" ) ) ).toBe( content );
					expect( downloadFails( "b/b-1.0.jar" ) ).toInclude( "MD5 validation failed" );
				});

				it( title="a Digest header passes or throws", body=function() {
					var content = "digest";
					serve( "a/a-1.0.jar", content, { "Digest": "sha-256=" & base64Of( content, "SHA-256" ) } );
					serve( "b/b-1.0.jar", content, { "Digest": "sha-256=" & base64Of( "something else", "SHA-256" ) } );
					expect( fileRead( download( "a/a-1.0.jar" ) ) ).toBe( content );
					expect( downloadFails( "b/b-1.0.jar" ) ).toInclude( "SHA256 validation failed" );
				});

				it( title="the hash of a header is used instead of the checksum files", body=function() {
					var content = "header before checksum files";
					serve( "a/a-1.0.jar", content, { "X-Checksum-SHA1": hex( "something else", "SHA-1" ) }, { "sha1": hex( content, "SHA-1" ) } );
					expect( downloadFails( "a/a-1.0.jar" ) ).toInclude( "SHA1 validation failed" );
				});

				it( title="an URL with a query string has no checksum files, only the headers count", body=function() {
					var content = "query";
					serve( "rest/download", content, { "X-Checksum-SHA1": hex( content, "SHA-1" ) } );
					serve( "rest/download.sha1", content, {} );
					expect( fileRead( download( "rest/download?allowRedirect=true" ) ) ).toBe( content );
					expect( requestsOf( ".sha" ) ).toBeEmpty();
				});
			});

			describe( "nothing to verify against", function() {

				it( title="a download without any hash succeeds", body=function() {
					var content = "no checksum";
					serve( "a/a-1.0.jar", content );
					expect( fileRead( download( "a/a-1.0.jar" ) ) ).toBe( content );
					expect( requestsOf( "a-1.0.jar." ) ).toBe( [ "GET /a/a-1.0.jar.sha512", "GET /a/a-1.0.jar.sha256", "GET /a/a-1.0.jar.sha1", "GET /a/a-1.0.jar.md5" ] );
				});

				it( title="a download without any hash is only a warning, logged once per URL", body=function() {
					var content = "no checksum";
					var messages = createObject( "java", "java.util.concurrent.CopyOnWriteArrayList" ).init();
					var log = createDynamicProxy( new LDEV5864.Log( messages ), [ "lucee.commons.io.log.Log" ] );
					var file = downloads & "unverified.jar";
					fileWrite( file, content );
					var res = getPageContext().getConfig().getResource( file );
					var dlUrl = createObject( "java", "java.net.URL" ).init( baseUrl & "a/a-1.0.jar" );
					serve( "a/a-1.0.jar", content );

					var result = DownloadVerifier.verify( dlUrl, javaCast( "null", "" ), res, javaCast( "null", "" ), true, true, log );
					expect( result.isVerified() ).toBeFalse();
					expect( messages.size() ).toBe( 1 );
					expect( messages.get( 0 ) ).toInclude( "3|download|" );
					expect( messages.get( 0 ) ).toInclude( baseUrl & "a/a-1.0.jar" );
					expect( fileExists( file ) ).toBeTrue();

					// a second download of the same artifact does not warn again
					result = DownloadVerifier.verify( dlUrl, javaCast( "null", "" ), res, javaCast( "null", "" ), true, true, log );
					expect( result.isVerified() ).toBeFalse();
					expect( messages.size() ).toBe( 1 );
				});

				it( title="the result tells the algorithm and the source when it is verified", body=function() {
					var content = "verified";
					var file = downloads & "verified.jar";
					fileWrite( file, content );
					var res = getPageContext().getConfig().getResource( file );
					var dlUrl = createObject( "java", "java.net.URL" ).init( baseUrl & "a/a-1.0.jar" );
					serve( "a/a-1.0.jar", content, {}, { "sha256": hex( content, "SHA-256" ) } );
					var result = DownloadVerifier.verify( dlUrl, javaCast( "null", "" ), res, javaCast( "null", "" ), true, true, javaCast( "null", "" ) );
					expect( result.isVerified() ).toBeTrue();
					expect( result.getAlgorithm() ).toBe( "sha256" );
					expect( result.getSource() ).toBe( baseUrl & "a/a-1.0.jar.sha256" );
				});

				it( title="a failing download is not hidden", body=function() {
					expect( downloadFails( "missing/missing-1.0.jar" ) ).toInclude( "404" );
				});
			});

			describe( "the Maven update provider", function() {

				it( title="getCore returns a core with a matching checksum", body=function() {
					var content = "core content";
					serve( "org/lucee/lucee/7.1.99.5864/lucee-7.1.99.5864.lco", content, {}, { "sha1": hex( content, "SHA-1" ) } );
					var is = provider( "lucee" ).getCore( toVersion( "7.1.99.5864" ) );
					try {
						expect( toString( is.readAllBytes(), "UTF-8" ) ).toBe( content );
					}
					finally {
						is.close();
					}
				});

				it( title="getCore throws for a core with a mismatching checksum", body=function() {
					serve( "org/lucee/lucee/7.1.99.5865/lucee-7.1.99.5865.lco", "core content", {}, { "sha1": hex( "something else", "SHA-1" ) } );
					expect( function() {
						provider( "lucee" ).getCore( toVersion( "7.1.99.5865" ) );
					}).toThrow( type="java.io.IOException", regex="SHA1 validation failed" );
				});

				it( title="getCore returns a core without a checksum", body=function() {
					var content = "unverified core";
					serve( "org/lucee/lucee/7.1.99.5866/lucee-7.1.99.5866.lco", content );
					var is = provider( "lucee" ).getCore( toVersion( "7.1.99.5866" ) );
					try {
						expect( toString( is.readAllBytes(), "UTF-8" ) ).toBe( content );
					}
					finally {
						is.close();
					}
				});

				it( title="getLoader throws for a loader with a mismatching checksum", body=function() {
					serve( "org/lucee/lucee/7.1.99.5867/lucee-7.1.99.5867.jar", "loader content", {}, { "sha256": hex( "something else", "SHA-256" ) } );
					expect( function() {
						provider( "lucee" ).getLoader( toVersion( "7.1.99.5867" ) );
					}).toThrow( type="java.io.IOException", regex="SHA256 validation failed" );
				});
			});

			describe( "the extension provider", function() {

				it( title="an extension with a matching checksum is installed in the local Maven repository", body=function() {
					var artifact = "ldev5864-ok-extension";
					var lex = zipContent( artifact );
					serve( "org/lucee/#artifact#/1.0.0.1/#artifact#-1.0.0.1.lex", lex, {}, { "sha1": hex( lex, "SHA-1" ) } );
					serve( "org/lucee/#artifact#/1.0.0.1/#artifact#-1.0.0.1.pom", pomContent( artifact ) );
					var artifactDir = localArtifactDir( artifact );
					if ( artifactDir.isDirectory() ) artifactDir.remove( true );
					try {
						var res = extensionProvider().getLEXResource( getPageContext().getConfig(), artifact, toVersion( "1.0.0.1" ) );
						expect( res.isFile() ).toBeTrue();
						expect( res.length() ).toBe( len( lex ) );
					}
					finally {
						if ( artifactDir.isDirectory() ) artifactDir.remove( true );
					}
				});

				it( title="an extension with a mismatching checksum throws and no file stays in the local Maven repository", body=function() {
					var artifact = "ldev5864-bad-extension";
					var lex = zipContent( artifact );
					serve( "org/lucee/#artifact#/1.0.0.1/#artifact#-1.0.0.1.lex", lex, {}, { "sha1": hex( "something else", "SHA-1" ) } );
					serve( "org/lucee/#artifact#/1.0.0.1/#artifact#-1.0.0.1.pom", pomContent( artifact ) );
					var artifactDir = localArtifactDir( artifact );
					if ( artifactDir.isDirectory() ) artifactDir.remove( true );
					try {
						var ep = extensionProvider();
						expect( function() {
							ep.getLEXResource( getPageContext().getConfig(), artifact, toVersion( "1.0.0.1" ) );
						}).toThrow( type="java.io.IOException", regex="SHA1 validation failed" );
						var cached = artifactDir.getRealResource( "1.0.0.1/#artifact#-1.0.0.1.lex" );
						expect( cached.exists() ).toBeFalse();
					}
					finally {
						if ( artifactDir.isDirectory() ) artifactDir.remove( true );
					}
				});

				it( title="an extension without a checksum is installed in the local Maven repository", body=function() {
					var artifact = "ldev5864-none-extension";
					var lex = zipContent( artifact );
					serve( "org/lucee/#artifact#/1.0.0.1/#artifact#-1.0.0.1.lex", lex );
					serve( "org/lucee/#artifact#/1.0.0.1/#artifact#-1.0.0.1.pom", pomContent( artifact ) );
					var artifactDir = localArtifactDir( artifact );
					if ( artifactDir.isDirectory() ) artifactDir.remove( true );
					try {
						var res = extensionProvider().getLEXResource( getPageContext().getConfig(), artifact, toVersion( "1.0.0.1" ) );
						expect( res.isFile() ).toBeTrue();
					}
					finally {
						if ( artifactDir.isDirectory() ) artifactDir.remove( true );
					}
				});
			});

			describe( "the loader", function() {

				it( title="a download with a matching checksum file is kept", body=function() {
					var content = "loader download";
					serve( "a/a-1.0.jar", content, {}, { "sha1": hex( content, "SHA-1" ) } );
					var target = downloads & "loader-ok.jar";
					loaderDownload( "a/a-1.0.jar", target );
					expect( fileRead( target ) ).toBe( content );
					expect( directoryList( downloads, false, "name", "loader-ok*" ) ).toBe( [ "loader-ok.jar" ] );
				});

				it( title="a download with a mismatching checksum file throws and nothing is left", body=function() {
					serve( "a/a-1.0.jar", "loader download", {}, { "sha256": hex( "something else", "SHA-256" ) } );
					var target = downloads & "loader-bad.jar";
					expect( function() {
						loaderDownload( "a/a-1.0.jar", target );
					}).toThrow( type="java.io.IOException", regex="SHA256 validation failed" );
					expect( directoryList( downloads, false, "name", "loader-bad*" ) ).toBeEmpty();
				});

				it( title="the checksum of a header is used, the strongest first", body=function() {
					var content = "loader header";
					serve( "a/a-1.0.jar", content, { "X-Checksum-SHA1": hex( content, "SHA-1" ), "X-Checksum-SHA256": hex( content, "SHA-256" ) } );
					serve( "b/b-1.0.jar", content, { "X-Checksum-SHA1": hex( content, "SHA-1" ), "X-Checksum-SHA256": hex( "something else", "SHA-256" ) } );
					var target = downloads & "loader-header.jar";
					loaderDownload( "a/a-1.0.jar", target );
					expect( fileRead( target ) ).toBe( content );
					expect( function() {
						loaderDownload( "b/b-1.0.jar", downloads & "loader-header-bad.jar" );
					}).toThrow( type="java.io.IOException", regex="SHA256 validation failed" );
				});

				it( title="a download without a checksum is kept", body=function() {
					var content = "loader unverified";
					serve( "a/a-1.0.jar", content );
					var target = downloads & "loader-none.jar";
					loaderDownload( "a/a-1.0.jar", target );
					expect( fileRead( target ) ).toBe( content );
				});

				it( title="the stream of a verified download throws for a mismatching checksum", body=function() {
					serve( "a/a-1.0.jar", "loader stream", {}, { "sha1": hex( "something else", "SHA-1" ) } );
					expect( function() {
						var conn = createObject( "java", "java.net.URL" ).init( baseUrl & "a/a-1.0.jar" ).openConnection();
						createObject( "java", "lucee.loader.util.DownloadVerifier" ).getVerifiedStream( conn );
					}).toThrow( type="java.io.IOException", regex="SHA1 validation failed" );
				});
			});
		});
	}

	/**
	 * the repository delivers the content and the checksum files, when headers are defined, they are
	 * delivered with the artifact (a checksum file that is not defined does not exist)
	 */
	private function serve( required string path, required content, struct headers = {}, struct checksums = {} ) {
		var file = repo & path;
		directoryCreate( getDirectoryFromPath( file ), true, true );
		fileWrite( file, content );
		for ( var algorithm in checksums ) {
			fileWrite( file & "." & algorithm, checksums[ algorithm ] );
		}
		if ( structIsEmpty( headers ) ) return;

		registerContext( "/" & path, 200, headers, content );
		for ( var algorithm in [ "sha512", "sha256", "sha1", "md5" ] ) {
			if ( structKeyExists( checksums, algorithm ) ) registerContext( "/" & path & "." & algorithm, 200, {}, checksums[ algorithm ] );
			else registerContext( "/" & path & "." & algorithm, 404, {}, "" );
		}
	}

	private function registerContext( required string path, required numeric status, required struct headers, required string body ) {
		var pairs = [];
		for ( var name in headers ) {
			pairs.append( name );
			pairs.append( headers[ name ] );
		}
		var h = createObject( "java", "com.sun.net.httpserver.Headers" ).of( javaCast( "string[]", pairs ) );
		var handler = createObject( "java", "com.sun.net.httpserver.HttpHandlers" ).of( javaCast( "int", status ), h, body );
		httpServer.createContext( path, handler ).getFilters().add( logFilter );
		headerContexts.append( path );
	}

	private string function hex( required content, required string algorithm ) {
		return lCase( hash( content, algorithm, "utf-8" ) );
	}

	private string function base64Of( required string content, required string algorithm ) {
		return toBase64( binaryDecode( hash( content, algorithm, "utf-8" ), "hex" ) );
	}

	/**
	 * the output filter logs a request only after the response was sent, so the client can be faster than the log.
	 * the server handles one exchange after the other (no executor), so once a last request is answered,
	 * all the requests before it are logged.
	 */
	private function syncRequestLog() {
		var conn = createObject( "java", "java.net.URL" ).init( baseUrl & "ldev5864-sync" ).openConnection();
		try {
			conn.setRequestMethod( "HEAD" );
			conn.getResponseCode();
		}
		finally {
			conn.disconnect();
		}
	}

	private array function requestsOf( required string part ) {
		syncRequestLog();
		var result = [];
		for ( var line in listToArray( requestLog.toString( "UTF-8" ), chr( 10 ) ) ) {
			var m = reMatch( '"(GET|HEAD) [^ ]+', line );
			if ( arrayLen( m ) && find( arguments.part, m[ 1 ] ) && !find( "ldev5864-sync", m[ 1 ] ) ) result.append( mid( m[ 1 ], 2, len( m[ 1 ] ) ) );
		}
		return result;
	}

	private string function download( required string path, string target = downloads & createUUID() & ".jar" ) {
		var dlUrl = createObject( "java", "java.net.URL" ).init( baseUrl & path );
		HTTPEngine.downloadToFile( dlUrl, createObject( "java", "java.io.File" ).init( target ) );
		return target;
	}

	private string function downloadFails( required string path, string target = downloads & createUUID() & ".jar" ) {
		try {
			download( arguments.path, arguments.target );
		}
		catch ( any e ) {
			return e.message;
		}
		fail( "the download of [#path#] should have failed" );
	}

	private function loaderDownload( required string path, required string target ) {
		var conn = createObject( "java", "java.net.URL" ).init( baseUrl & path ).openConnection();
		conn.setRequestMethod( "GET" );
		conn.connect();
		createObject( "java", "lucee.loader.util.DownloadVerifier" ).download( conn, createObject( "java", "java.io.File" ).init( target ) );
	}

	private function repositories() {
		var Repository = createObject( "java", "lucee.runtime.config.maven.MavenUpdateProvider$Repository" );
		// no caching, every call asks the repository
		var r = Repository.init( "LDEV-5864", baseUrl, Repository.TYPE_ALL, Repository.TIMEOUT_ZERO, Repository.TIMEOUT_ZERO, getPageContext().getConfig().getResource( cache ) );
		var repos = createObject( "java", "java.lang.reflect.Array" ).newInstance( r.getClass(), 1 );
		repos[ 1 ] = r;
		return repos;
	}

	private function provider( required string artifact ) {
		return createObject( "java", "lucee.runtime.config.maven.MavenUpdateProvider" ).init( repositories(), "org.lucee", arguments.artifact );
	}

	// the constructor of the ExtensionProvider that takes the repositories is private
	private function extensionProvider() {
		var repos = repositories();
		var ConfigClass = createObject( "java", "java.lang.Class" ).forName( "lucee.runtime.config.Config", false, repos.getClass().getComponentType().getClassLoader() );
		var types = createObject( "java", "java.lang.reflect.Array" ).newInstance( createObject( "java", "java.lang.Class" ).getClass(), 3 );
		types[ 1 ] = ConfigClass;
		types[ 2 ] = createObject( "java", "java.lang.String" ).getClass();
		types[ 3 ] = repos.getClass();
		var c = repos.getClass().getComponentType().getClassLoader().loadClass( "lucee.runtime.config.maven.ExtensionProvider" ).getDeclaredConstructor( types );
		c.setAccessible( true );
		return c.newInstance( [ getPageContext().getConfig(), "org.lucee", repos ] );
	}

	private function localArtifactDir( required string artifact ) {
		var POM = createObject( "java", "lucee.runtime.mvn.POM" );
		return POM.localArtifactDir( getPageContext().getConfig().getMavenDir(), "org.lucee", arguments.artifact );
	}

	private string function pomContent( required string artifact ) {
		return "<project><groupId>org.lucee</groupId><artifactId>#arguments.artifact#</artifactId><version>1.0.0.1</version><packaging>lex</packaging></project>";
	}

	private function zipContent( required string artifact ) {
		var src = root & "lexsrc-" & createUUID() & "/";
		var file = root & arguments.artifact & ".lex";
		directoryCreate( src & "META-INF", true );
		fileWrite( src & "META-INF/MANIFEST.MF", "Manifest-Version: 1.0#chr(10)#name: #arguments.artifact##chr(10)#" );
		compress( "zip", src, file, false );
		directoryDelete( src, true );
		var bytes = fileReadBinary( file );
		fileDelete( file );
		return bytes;
	}

	private function toVersion( required string version ) {
		return createObject( "java", "lucee.runtime.config.maven.Version" ).parseVersion( version );
	}
}
