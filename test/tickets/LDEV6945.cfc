component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults, testBox ) {
		describe( "LDEV-6945 TemplateLine does not truncate a Windows path to the drive letter", function() {

			it( title="a bare Windows path stays the template and has no line", body=function() {
				var tl = templateLine( "D:\app\test.cfc" );
				expect( tl.template ).toBe( "D:\app\test.cfc" );
				expect( tl.line ).toBe( 0 );
			});

			it( title="a Windows path with a line suffix still splits template and line", body=function() {
				var tl = templateLine( "D:\app\test.cfc:12" );
				expect( tl.template ).toBe( "D:\app\test.cfc" );
				expect( tl.line ).toBe( 12 );
			});

			it( title="a Unix path with a line suffix still splits template and line", body=function() {
				var tl = templateLine( "/app/test.cfc:12" );
				expect( tl.template ).toBe( "/app/test.cfc" );
				expect( tl.line ).toBe( 12 );
			});

			it( title="a Unix path with no line suffix stays the template", body=function() {
				var tl = templateLine( "/app/test.cfc" );
				expect( tl.template ).toBe( "/app/test.cfc" );
				expect( tl.line ).toBe( 0 );
			});

		});
	}

	private any function templateLine( required string templateAndLine ) {
		var TemplateLine = createObject( "java", "lucee.commons.io.SystemUtil$TemplateLine" );
		return TemplateLine.init( arguments.templateAndLine );
	}

}
