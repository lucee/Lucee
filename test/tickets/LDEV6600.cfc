component extends="org.lucee.cfml.test.LuceeTestCase" labels="asm" {

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

		});
	}
}
