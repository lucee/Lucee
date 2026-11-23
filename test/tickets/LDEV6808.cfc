// LDEV-6808 extensions can declare dependencies on other extensions (manifest key require-extension)
component extends="org.lucee.cfml.test.LuceeTestCase" labels="extension" {

	function beforeAll() {
		variables.pw = request.SERVERADMINPASSWORD;
		variables.lex = new LDEV6829.Lex();
		variables.dir = getTempDirectory() & "ldev6808-" & createUUID() & "/";
		directoryCreate( variables.dir, true );
		variables.ids = [];
	}

	function afterAll() {
		// leave the server like we found it, the ids are in creation order (required extensions first), removed in reverse
		var i = arrayLen( variables.ids );
		while ( i > 0 ) {
			try {
				remove( variables.ids[ i ], true );
			}
			catch ( any e ) {
			}
			i--;
		}
		if ( directoryExists( variables.dir ) ) directoryDelete( variables.dir, true );
	}

	function run( testResults, testBox ) {
		describe( "LDEV-6808 extensions can declare dependencies on other extensions", function() {

			it( title="a required extension is installed together with the extension", body=function( currentSpec ) {
				var dep = create( "dep", "1.0.0.1" );
				var main = create( "main", "1.0.0.1", "path=" & dep.file );
				expect( isInstalled( dep.id ) ).toBeFalse();

				install( main );

				expect( isInstalled( main.id ) ).toBeTrue();
				expect( isInstalled( dep.id ) ).toBeTrue();
				expect( extensionInfo( main.id ).requiredExtensions ).toHaveLength( 1 );
				expect( extensionInfo( dep.id ).requiredBy ).toBe( [ main.id ] );
			});

			it( title="the required extensions of a required extension are installed first", body=function( currentSpec ) {
				var c = create( "c", "1.0.0.1" );
				var b = create( "b", "1.0.0.1", "path=" & c.file );
				var a = create( "a", "1.0.0.1", "path=" & b.file );

				install( a );

				expect( isInstalled( a.id ) ).toBeTrue();
				expect( isInstalled( b.id ) ).toBeTrue();
				expect( isInstalled( c.id ) ).toBeTrue();
			});

			it( title="an extension that cannot be resolved fails the install and nothing is installed", body=function( currentSpec ) {
				var other = create( "other", "1.0.0.1" );
				var main = create( "main", "1.0.0.1", "path=" & other.file & "," & createUUID() );

				var message = "";
				try {
					install( main );
				}
				catch ( any e ) {
					message = e.message;
				}
				expect( message ).toInclude( "required extension" );
				expect( isInstalled( main.id ) ).toBeFalse();
				// the one that could be resolved is not installed either
				expect( isInstalled( other.id ) ).toBeFalse();
			});

			it( title="a cycle in the required extensions is an error", body=function( currentSpec ) {
				var idA = createUUID();
				var idB = createUUID();
				var fileA = variables.dir & "cycle-a.lex";
				var fileB = variables.dir & "cycle-b.lex";
				var a = create( "cycle-a", "1.0.0.1", "path=" & fileB, idA, fileA );
				var b = create( "cycle-b", "1.0.0.1", "path=" & fileA, idB, fileB );

				var message = "";
				try {
					install( a );
				}
				catch ( any e ) {
					message = e.message;
				}
				expect( message ).toInclude( "cycle" );
				expect( isInstalled( a.id ) ).toBeFalse();
				expect( isInstalled( b.id ) ).toBeFalse();
			});

			it( title="an installed version that is too old is updated, a newer one is left alone", body=function( currentSpec ) {
				var id = createUUID();
				var dep1 = create( "dep-v", "1.0.0.1", "", id );
				var dep5 = create( "dep-v", "1.0.0.5", "", id );
				// requires at least 1.0.0.5
				var main = create( "main-v", "1.0.0.1", "path=" & dep5.file );
				// requires at least 1.0.0.1
				var main2 = create( "main-v2", "1.0.0.1", "path=" & dep1.file );

				install( dep1 );
				expect( extensionInfo( id ).version ).toBe( "1.0.0.1" );
				install( main );
				expect( extensionInfo( id ).version ).toBe( "1.0.0.5" );

				// a newer version than the required one is fine
				install( main2 );
				expect( extensionInfo( id ).version ).toBe( "1.0.0.5" );
			});

			it( title="the higher minimum version wins when two extensions need the same extension", body=function( currentSpec ) {
				var id = createUUID();
				var c1 = create( "c-d", "1.0.0.1", "", id );
				var c5 = create( "c-d", "1.0.0.5", "", id );
				// b needs at least 1.0.0.1, a needs b and at least 1.0.0.5
				var b = create( "b-d", "1.0.0.1", "path=" & c1.file );
				var a = create( "a-d", "1.0.0.1", "path=" & b.file & ",path=" & c5.file );

				install( a );

				expect( isInstalled( a.id ) ).toBeTrue();
				expect( isInstalled( b.id ) ).toBeTrue();
				expect( extensionInfo( id ).version ).toBe( "1.0.0.5" );
			});

			it( title="a required extension cannot be removed, unless forced", body=function( currentSpec ) {
				var dep = create( "dep-r", "1.0.0.1" );
				var main = create( "main-r", "1.0.0.1", "path=" & dep.file );
				install( main );

				var message = "";
				try {
					remove( dep.id, false );
				}
				catch ( any e ) {
					message = e.message;
				}
				expect( message ).toInclude( "required by" );
				expect( isInstalled( dep.id ) ).toBeTrue();

				remove( dep.id, true );
				expect( isInstalled( dep.id ) ).toBeFalse();
				// the extension that needs it can be removed
				remove( main.id, false );
				expect( isInstalled( main.id ) ).toBeFalse();
			});

			it( title="a required extension cannot be replaced by a version that does not fit", body=function( currentSpec ) {
				var id = createUUID();
				var dep1 = create( "dep-u", "1.0.0.1", "", id );
				var dep0 = create( "dep-u", "1.0.0.0", "", id );
				var main = create( "main-u", "1.0.0.1", "path=" & dep1.file );
				install( main );

				var message = "";
				try {
					install( dep0 );
				}
				catch ( any e ) {
					message = e.message;
				}
				expect( message ).toInclude( "requires at least" );
				expect( extensionInfo( id ).version ).toBe( "1.0.0.1" );
			});

			it( title="an extension that does not fit the core is rejected before any required extension is installed", body=function( currentSpec ) {
				var dep = create( "dep-c", "1.0.0.1" );
				var main = create( "main-c", "1.0.0.1", "path=" & dep.file, createUUID(), "", { "lucee-core-version": "99.0.0.0" } );

				var failed = false;
				try {
					install( main );
				}
				catch ( any e ) {
					failed = true;
				}
				expect( failed ).toBeTrue();
				expect( isInstalled( main.id ) ).toBeFalse();
				expect( isInstalled( dep.id ) ).toBeFalse();
			});

			it( title="a required extension that does not fit the core is rejected before any other is installed", body=function( currentSpec ) {
				var ok = create( "dep-ok", "1.0.0.1" );
				var bad = create( "dep-bad", "1.0.0.1", "", createUUID(), "", { "lucee-core-version": "99.0.0.0" } );
				var main = create( "main-d", "1.0.0.1", "path=" & ok.file & ",path=" & bad.file );

				var failed = false;
				try {
					install( main );
				}
				catch ( any e ) {
					failed = true;
				}
				expect( failed ).toBeTrue();
				expect( isInstalled( main.id ) ).toBeFalse();
				expect( isInstalled( ok.id ) ).toBeFalse();
			});

			it( title="a required extension that is declared by its id is protected without resolving anything", body=function( currentSpec ) {
				var dep = create( "dep-i", "1.0.0.1" );
				install( dep );
				var main = create( "main-i", "1.0.0.1", dep.id & ";version=1.0.0.1" );
				install( main );

				expect( extensionInfo( dep.id ).requiredBy ).toBe( [ main.id ] );
				var message = "";
				try {
					remove( dep.id, false );
				}
				catch ( any e ) {
					message = e.message;
				}
				expect( message ).toInclude( "required by" );
			});

			it( title="an extension defined in the configuration wins over an installed extension that needs a newer version", body=function( currentSpec ) {
				var id = createUUID();
				var dep1 = create( "dep-w", "1.0.0.1", "", id );
				var dep0 = create( "dep-w", "1.0.0.0", "", id );
				var main = create( "main-w", "1.0.0.1", "path=" & dep1.file );
				install( main );

				var deps = createObject( "java", "lucee.runtime.extension.ExtensionDependencies" );
				deps.setConfigured( true );
				try {
					install( dep0 );
				}
				finally {
					deps.setConfigured( false );
				}
				expect( extensionInfo( id ).version ).toBe( "1.0.0.0" );
			});

			it( title="the files of the deploy folder are deployed in dependency order", body=function( currentSpec ) {
				var cs = createObject( "java", "lucee.runtime.config.ConfigUtil" ).getConfigServerImpl( getPageContext().getConfig() );
				var deployDir = cs.getDeployDirectory().getAbsolutePath() & "/";
				var b = create( "b-deploy", "1.0.0.1", "", createUUID(), deployDir & "b-ldev6808-" & createUUID() & ".lex" );
				// the id of b cannot be resolved by a provider, b is only found next to a
				var a = create( "a-deploy", "1.0.0.1", b.id & ";version=1.0.0.1", createUUID(), deployDir & "a-ldev6808-" & createUUID() & ".lex" );

				try {
					createObject( "java", "lucee.runtime.config.DeployHandler" ).deploy( cs, cs.getLog( "deploy" ), true );
				}
				finally {
					if ( fileExists( a.file ) ) fileDelete( a.file );
					if ( fileExists( b.file ) ) fileDelete( b.file );
					var failed = deployDir & "failed-to-deploy/";
					if ( fileExists( failed & getFileFromPath( a.file ) ) ) fileDelete( failed & getFileFromPath( a.file ) );
					if ( fileExists( failed & getFileFromPath( b.file ) ) ) fileDelete( failed & getFileFromPath( b.file ) );
				}
				expect( isInstalled( b.id ) ).toBeTrue();
				expect( isInstalled( a.id ) ).toBeTrue();
			});

			it( title="an extension without required extensions works as before", body=function( currentSpec ) {
				var plain = create( "plain", "1.0.0.1" );
				install( plain );
				expect( isInstalled( plain.id ) ).toBeTrue();
				expect( extensionInfo( plain.id ).requiredExtensions ).toHaveLength( 0 );
				remove( plain.id, false );
				expect( isInstalled( plain.id ) ).toBeFalse();
			});

		});
	}

	/**
	 * creates an extension file, requires is the definition of the extensions it needs (key require-extension)
	 */
	private struct function create( required string name, required string version, string requires = "", string id = createUUID(), string file = "", struct attrs = {} ) {
		if ( !len( arguments.file ) ) arguments.file = variables.dir & arguments.name & "-" & arguments.version & "-" & createUUID() & ".lex";
		var attrs = duplicate( arguments.attrs );
		if ( len( arguments.requires ) ) attrs[ "require-extension" ] = arguments.requires;
		variables.lex.createLex( arguments.file, "", "", "LDEV6808 " & arguments.name, arguments.id, arguments.version, attrs, {} );
		if ( !arrayFind( variables.ids, arguments.id ) ) arrayAppend( variables.ids, arguments.id );
		return { id: arguments.id, version: arguments.version, file: arguments.file };
	}

	private void function install( required struct ext ) {
		admin action="updateRHExtension" type="server" password=variables.pw source=arguments.ext.file;
	}

	private boolean function isInstalled( required string id ) {
		return structCount( extensionInfo( arguments.id ) ) > 0;
	}

	private void function remove( required string id, required boolean force ) {
		admin action="removeRHExtension" type="server" password=variables.pw id=arguments.id force=arguments.force;
	}

}
