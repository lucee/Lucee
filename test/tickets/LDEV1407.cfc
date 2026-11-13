component extends="org.lucee.cfml.test.LuceeTestCase" labels="bytecode" {

	function run( testResults, testBox ) {
		describe( "LDEV-1407: SourceNameClassVisitor passes an invalid api version to ASM", function() {

			it( title="reading the source info of a java class must not throw 'Unsupported api'", body=function( currentSpec ) {
				var asmUtil = createObject( "java", "lucee.transformer.bytecode.util.ASMUtil" );
				var clazz = createObject( "java", "lucee.runtime.type.util.ListUtil" ).getClass();

				// the class is not a CFML template, so there is no source info to return, but reading it must not fail
				var info = asmUtil.getSourceInfo( getPageContext().getConfig(), clazz, false );
				expect( isNull( info ) ).toBeTrue();
			});

		});
	}

}
