component extends="org.lucee.cfml.test.LuceeTestCase" labels="extension" {

	function beforeAll() {
		variables.dir = getTempDirectory() & "LDEV6713-" & createUUID() & "/";
		directoryCreate( dir );
	}

	function afterAll() {
		if ( directoryExists( dir ) ) directoryDelete( dir, true );
	}

	function run( testResults, testBox ) {
		describe( "removeRHExtension of an extension that is only available locally", function() {

			it( "removes the extension by id", function() {
				var id = createUUID();
				var lex = createExtension( id, "1.0.0.1" );
				admin action="updateRHExtension" type="server" password=request.SERVERADMINPASSWORD source=lex;
				expect( isInstalled( id ) ).toBeTrue();
				admin action="removeRHExtension" type="server" password=request.SERVERADMINPASSWORD id=id;
				expect( isInstalled( id ) ).toBeFalse();
			});

			it( "removes only the installed extension, other extensions are left alone", function() {
				var id = createUUID();
				var otherId = createUUID();
				admin action="updateRHExtension" type="server" password=request.SERVERADMINPASSWORD source=createExtension( id, "1.0.0.1" );
				admin action="updateRHExtension" type="server" password=request.SERVERADMINPASSWORD source=createExtension( otherId, "1.0.0.1" );
				admin action="removeRHExtension" type="server" password=request.SERVERADMINPASSWORD id=id;
				expect( isInstalled( id ) ).toBeFalse();
				expect( isInstalled( otherId ) ).toBeTrue();
				admin action="removeRHExtension" type="server" password=request.SERVERADMINPASSWORD id=otherId;
				expect( isInstalled( otherId ) ).toBeFalse();
			});

			it( "fails with a clear error for an extension that is not installed", function() {
				var id = createUUID();
				expect( function() {
					admin action="removeRHExtension" type="server" password=request.SERVERADMINPASSWORD id=id;
				}).toThrow( regex="^((?!NullPointerException).)*$" );
			});
		});
	}

	private string function createExtension( required string id, required string version ) {
		var src = dir & "src-" & id & "/";
		directoryCreate( src & "META-INF", true );
		fileWrite( src & "META-INF/MANIFEST.MF", 'Manifest-Version: 1.0#chr(10)#id: "#id#"#chr(10)#version: "#version#"#chr(10)#name: "ldev6713-#lCase( left( id, 8 ) )#"#chr(10)#description: "test extension"#chr(10)#' );
		var lex = dir & "ldev6713-" & id & ".lex";
		zip action="zip" source=src file=lex;
		return lex;
	}

	private boolean function isInstalled( required string id ) {
		var target = arguments.id;
		admin action="getRHExtensions" type="server" password=request.SERVERADMINPASSWORD returnVariable="local.exts";
		return queryFilter( local.exts, function( row ) { return row.id == target; } ).recordCount > 0;
	}
}
