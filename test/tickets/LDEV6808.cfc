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
		// leave the server like we found it, the extensions that need others first
		loop array=variables.ids item="local.id" {
			try {
				remove( id, true );
			}
			catch ( any e ) {
			}
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
	private struct function create( required string name, required string version, string requires = "", string id = createUUID(), string file = "" ) {
		if ( !len( arguments.file ) ) arguments.file = variables.dir & arguments.name & "-" & arguments.version & "-" & createUUID() & ".lex";
		var attrs = {};
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
