component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults, testBox ) {
		describe( "compileToBytecode()", function() {

			it( "compiles a single file and keeps dot-prefixed source", function() {
				var dir = getDirectoryFromPath( getCurrentTemplatePath() ) & "LDEV6378-single-" & createUUID() & "/";
				directoryCreate( dir );
				var mainFile = dir & "main.cfm";
				fileWrite( mainFile, '<cfset compileToBytecodeMain = "ok">' );

				try {
					var result = compileToBytecode( mainFile, true, "", false );
					expect( arrayLen( result.compiled ) ).toBe( 1 );
					expect( structCount( result.errors ) ).toBe( 0 );
					expect( fileExists( dir & ".main.cfm" ) ).toBeTrue();
					expect( fileRead( dir & ".main.cfm" ) ).toInclude( "compileToBytecodeMain" );

					var classUtil = createObject( "java", "lucee.commons.lang.ClassUtil" );
					expect( classUtil.isBytecode( fileReadBinary( mainFile ) ) ).toBeTrue();
				}
				finally {
					if ( directoryExists( dir ) ) directoryDelete( dir, true );
				}
			} );

			it( "compiles a directory recursively", function() {
				var dir = getDirectoryFromPath( getCurrentTemplatePath() ) & "LDEV6378-dir-" & createUUID() & "/";
				directoryCreate( dir );
				var subDir = dir & "sub/";
				directoryCreate( subDir );
				fileWrite( dir & "main.cfm", '<cfset x = 1>' );
				fileWrite( subDir & "sub.cfm", '<cfset y = 2>' );

				try {
					var result = compileToBytecode( dir, false, "", true );
					expect( arrayLen( result.compiled ) ).toBe( 2 );
					expect( structCount( result.errors ) ).toBe( 0 );

					var classUtil = createObject( "java", "lucee.commons.lang.ClassUtil" );
					expect( classUtil.isBytecode( fileReadBinary( dir & "main.cfm" ) ) ).toBeTrue();
					expect( classUtil.isBytecode( fileReadBinary( subDir & "sub.cfm" ) ) ).toBeTrue();
				}
				finally {
					if ( directoryExists( dir ) ) directoryDelete( dir, true );
				}
			} );

			it( "skips already compiled files when run again", function() {
				var dir = getDirectoryFromPath( getCurrentTemplatePath() ) & "LDEV6378-skip-" & createUUID() & "/";
				directoryCreate( dir );
				var mainFile = dir & "main.cfm";
				fileWrite( mainFile, '<cfset z = 3>' );

				try {
					compileToBytecode( mainFile, false, "", false );
					var result = compileToBytecode( dir, false, "", false );
					expect( arrayLen( result.compiled ) ).toBe( 0 );
					expect( arrayLen( result.skipped ) ).toBe( 1 );
				}
				finally {
					if ( directoryExists( dir ) ) directoryDelete( dir, true );
				}
			} );

			it( "encrypts bytecode with a private key", function() {
				var dir = getDirectoryFromPath( getCurrentTemplatePath() ) & "LDEV6378-enc-" & createUUID() & "/";
				directoryCreate( dir );
				var encFile = dir & "enc.cfm";
				fileWrite( encFile, '<cfset compileToBytecodeEnc = "secret">' );

				try {
					var rsa = createObject( "java", "lucee.commons.digest.RSA" );
					var kp = rsa.createKeyPair();
					var privateKey = rsa.toString( kp.getPrivate() );

					var result = compileToBytecode( encFile, true, privateKey, false );
					expect( arrayLen( result.compiled ) ).toBe( 1 );
					expect( structCount( result.errors ) ).toBe( 0 );

					var classUtil = createObject( "java", "lucee.commons.lang.ClassUtil" );
					var bytes = fileReadBinary( encFile );
					var is = createObject( "java", "java.io.ByteArrayInputStream" ).init( bytes );
					expect( classUtil.isEncryptedBytecode( is ) ).toBeTrue();
				}
				finally {
					if ( directoryExists( dir ) ) directoryDelete( dir, true );
				}
			} );
		} );

		describe( "restoreToSource()", function() {

			it( "restores source from a dot-prefixed backup", function() {
				var dir = getDirectoryFromPath( getCurrentTemplatePath() ) & "LDEV6378-restore-" & createUUID() & "/";
				directoryCreate( dir );
				var mainFile = dir & "main.cfm";
				var source = '<cfset restoreToSourceTest = "ok">';
				fileWrite( mainFile, source );

				try {
					compileToBytecode( mainFile, true, "", false );
					var result = restoreToSource( mainFile, false );
					expect( arrayLen( result.restored ) ).toBe( 1 );
					expect( structCount( result.errors ) ).toBe( 0 );
					expect( fileExists( dir & ".main.cfm" ) ).toBeFalse();
					expect( fileRead( mainFile ) ).toBe( source );

					var classUtil = createObject( "java", "lucee.commons.lang.ClassUtil" );
					expect( classUtil.isBytecode( fileReadBinary( mainFile ) ) ).toBeFalse();
				}
				finally {
					if ( directoryExists( dir ) ) directoryDelete( dir, true );
				}
			} );

			it( "restores a directory recursively", function() {
				var dir = getDirectoryFromPath( getCurrentTemplatePath() ) & "LDEV6378-restore-dir-" & createUUID() & "/";
				directoryCreate( dir );
				var subDir = dir & "sub/";
				directoryCreate( subDir );
				var mainSource = '<cfset restoreMain = 1>';
				var subSource = '<cfset restoreSub = 2>';
				fileWrite( dir & "main.cfm", mainSource );
				fileWrite( subDir & "sub.cfm", subSource );

				try {
					compileToBytecode( dir, true, "", true );
					var result = restoreToSource( dir, true );
					expect( arrayLen( result.restored ) ).toBe( 2 );
					expect( structCount( result.errors ) ).toBe( 0 );
					expect( fileRead( dir & "main.cfm" ) ).toBe( mainSource );
					expect( fileRead( subDir & "sub.cfm" ) ).toBe( subSource );
				}
				finally {
					if ( directoryExists( dir ) ) directoryDelete( dir, true );
				}
			} );
		} );
	}

}
