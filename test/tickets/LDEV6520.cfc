component extends="org.lucee.cfml.test.LuceeTestCase" labels="extensions" {

	function run( testResults, testBox ) {
		describe( "Test suite for LDEV-6520 (bundle markdown and smb extensions on 8.0)", function() {

			it( title="markdown-extension Require-Extension entry declares since=8.0.0.0", body=function( currentSpec ) {
				var ed = findRequired( "markdown-extension" );
				expect( isNull( ed ) ).toBeFalse( "markdown-extension missing from Require-Extension" );
				expect( isNull( ed.getSince() ) ).toBeFalse( "markdown-extension has no since= (7.1->8.0 minor update will not install it)" );
				expect( ed.getSince().toString() ).toBe( "8.0.0.0" );
			});

			it( title="smb-extension Require-Extension entry declares since=8.0.0.0", body=function( currentSpec ) {
				var ed = findRequired( "smb-extension" );
				expect( isNull( ed ) ).toBeFalse( "smb-extension missing from Require-Extension" );
				expect( isNull( ed.getSince() ) ).toBeFalse( "smb-extension has no since=" );
				expect( ed.getSince().toString() ).toBe( "8.0.0.0" );
			});

			it( title="MarkdownToHTML() is available when markdown extension is installed", body=function( currentSpec ) {
				// the extension provides MarkdownToHTML(); "markdown" is only an alias of its first argument, not a function
				expect( function() { getFunctionData( "MarkdownToHTML" ); } ).notToThrow();
				expect( markdownToHTML( "## Hello" ) ).toInclude( "<h2" );
			});

			it( title="smb resource provider is registered when smb extension is installed", body=function( currentSpec ) {
				expect( getPageContext().getConfig().hasResourceProvider( "smb" ) ).toBeTrue(
					"smb scheme resource provider missing (smb-extension not installed/bundled?)"
				);
			});

		});
	}

	private any function findRequired( required string artifactId ) {
		var info = getPageContext().getConfig().getFactory().getEngine().getInfo();
		var it = info.getRequiredExtension().iterator();
		while ( it.hasNext() ) {
			var ed = it.next();
			if ( ( ed.getArtifactId() ?: "" ) == arguments.artifactId ) return ed;
		}
		return javaCast( "null", "" );
	}
}
