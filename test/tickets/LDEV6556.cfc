component extends="org.lucee.cfml.test.LuceeTestCase" labels="maven" {

	function beforeAll() {
		variables.base = getTempDirectory() & "ldev6556-" & createUUID() & "/";
		variables.bundle = variables.base & "bundle/";
		variables.repo = variables.base & "repo/";
		directoryCreate( variables.bundle & "maven/org/example/demo/1.0", true, true );
		variables.rel = "org/example/demo/1.0/demo-1.0-tests.jar";
		variables.badRel = "org/example/demo/1.0/bad-1.0.jar";
		_addArtifact( variables.rel, "hello" );
		_addArtifact( variables.badRel, "bad" );
		fileWrite( variables.bundle & "maven/" & variables.badRel & ".sha1", "0000000000000000000000000000000000000000" );
		fileWrite( variables.bundle & "maven/.index", variables.rel & ";" & variables.rel & ".sha1;" & variables.badRel & ";" & variables.badRel & ".sha1" );
	}

	function afterAll() {
		if ( directoryExists( variables.base ) ) directoryDelete( variables.base, true );
	}

	function run( testResults, testBox ) {
		describe( "LDEV-6556 deploy bundled maven artifacts", function() {

			it( "deploys artifacts with classifier, rejects checksum mismatch", function() {
				var count = _deploy( variables.repo );
				expect( count ).toBe( 1 );
				expect( fileRead( variables.repo & variables.rel ) ).toBe( "hello" );
				expect( fileExists( variables.repo & variables.rel & ".sha1" ) ).toBeTrue();
				expect( fileExists( variables.repo & variables.badRel ) ).toBeFalse();
			});

			it( "does not redeploy matching files", function() {
				expect( _deploy( variables.repo ) ).toBe( 0 );
			});

			it( "replaces a file with a mismatching checksum and deletes nothing", function() {
				fileWrite( variables.repo & variables.rel, "tampered" );
				fileWrite( variables.repo & "other.txt", "keep" );
				expect( _deploy( variables.repo ) ).toBe( 1 );
				expect( fileRead( variables.repo & variables.rel ) ).toBe( "hello" );
				expect( fileExists( variables.repo & "other.txt" ) ).toBeTrue();
			});

			it( "supports a custom repository path", function() {
				var custom = variables.base & "custom/repo/";
				expect( _deploy( custom ) ).toBe( 1 );
				expect( fileExists( custom & variables.rel ) ).toBeTrue();
			});

			it( "does nothing for a core without maven folder", function() {
				var empty = variables.base & "empty/";
				directoryCreate( empty );
				expect( _deploy( variables.base & "r2/", empty ) ).toBe( 0 );
				expect( directoryExists( variables.base & "r2/" ) ).toBeFalse();
			});
		});
	}

	private void function _addArtifact( required string rel, required string content ) {
		fileWrite( variables.bundle & "maven/" & arguments.rel, arguments.content );
		fileWrite( variables.bundle & "maven/" & arguments.rel & ".sha1", lcase( hash( arguments.content, "SHA-1" ) ) );
	}

	private numeric function _deploy( required string repoPath, string bundlePath=variables.bundle ) {
		var jarUrl = createObject( "java", "java.io.File" ).init( arguments.bundlePath ).toURI().toURL();
		var cl = createObject( "java", "java.net.URLClassLoader" ).init( javaCast( "java.net.URL[]", [ jarUrl ] ) );
		var pc = getPageContext();
		var repo = createObject( "java", "lucee.commons.io.res.util.ResourceUtil" ).toResourceNotExisting( pc, arguments.repoPath );
		var log = pc.getConfig().getLog( "deploy" );
		return createObject( "java", "lucee.runtime.mvn.MavenDeployer" ).deployBundled( repo, cl, log );
	}
}
