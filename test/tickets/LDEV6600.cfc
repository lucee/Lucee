component extends="org.lucee.cfml.test.LuceeTestCase" labels="asm" {

	function beforeAll() {
		variables.dir = getDirectoryFromPath( getCurrentTemplatePath() ) & "LDEV6600/";
		if ( !directoryExists( variables.dir ) ) directoryCreate( variables.dir );
	}

	function afterAll() {
		if ( directoryExists( variables.dir ) ) directoryDelete( variables.dir, true );
	}

	function run( testResults, testBox ) {
		describe( "LDEV-6600 ASM 9.11.1, Java 28 support", function() {

			it( title="the max supported bytecode version is the highest version ASM knows", body=function( currentSpec ) {
				var cl = getPageContext().getClass().getClassLoader();
				var Class = createObject( "java", "java.lang.Class" );
				var opcodes = Class.forName( "org.objectweb.asm.Opcodes", false, cl );
				var highest = 0;
				for ( var field in opcodes.getFields() ) {
					if ( reFind( "^V\d+$", field.getName() ) ) highest = max( highest, field.getInt( nullValue() ) );
				}
				var maxVersion = createObject( "java", "lucee.transformer.bytecode.util.ASMUtil" ).getMaxVersion();
				expect( maxVersion ).toBe( highest );
				// at least Java 28
				expect( maxVersion - 44 ).toBeGTE( 28 );
			});

			// ASM 9.11 limits the frame computation, a template with many try/catch blocks failed with "Too many operations"
			it( title="a template with many try/catch blocks compiles", body=function( currentSpec ) {
				var blocks = 150;
				var src = [ "<cfscript>", "ldev6600X = 0;", "ldev6600Count = 0;" ];
				for ( var i = 1; i <= blocks; i++ ) {
					arrayAppend( src, "try { if ( ldev6600X == -#i# ) { ldev6600Y = #i#; } else { ldev6600Count++; } } catch ( any e ) { ldev6600X = #i#; }" );
				}
				arrayAppend( src, "</cfscript>" );
				var name = "tryCatch#replace( createUUID(), "-", "", "all" )#.cfm";
				fileWrite( variables.dir & name, arrayToList( src, chr( 10 ) ) );

				include "LDEV6600/#name#";
				expect( ldev6600Count ).toBe( blocks );
			});

		});
	}
}
