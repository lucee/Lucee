component extends="org.lucee.cfml.test.LuceeTestCase" labels="osgi" {

	function beforeAll() {
		// OSGiUtil is a class of the core bundle
		variables.core = createObject( "java", "lucee.runtime.osgi.OSGiUtil" ).getClass().getClassLoader().getBundle();
	}

	private boolean function canLoad( required string className ) {
		try {
			variables.core.loadClass( arguments.className );
			return true;
		}
		catch ( any e ) {
			// only a class that does not exist is a "no", anything else is an error in the test
			if ( findNoCase( "ClassNotFoundException", e.type & " " & e.message & " " & e.stackTrace ) ) return false;
			rethrow;
		}
	}

	function run( testResults, testBox ) {
		describe( "ASM is shaded into the core", function() {

			it( title="the core has the ASM classes only under the relocated name", body=function( currentSpec ) {
				expect( canLoad( "lucee.shaded.asm.ClassWriter" ) ).toBeTrue();
				expect( canLoad( "lucee.shaded.asm.commons.GeneratorAdapter" ) ).toBeTrue();
				expect( canLoad( "lucee.shaded.asm.tree.ClassNode" ) ).toBeTrue();
				expect( canLoad( "lucee.shaded.asm.util.TraceClassVisitor" ) ).toBeTrue();
				expect( canLoad( "lucee.shaded.asm.tree.analysis.Analyzer" ) ).toBeTrue();
				expect( canLoad( "org.objectweb.asm.ClassWriter" ) ).toBeFalse();
				expect( canLoad( "org.objectweb.asm.commons.GeneratorAdapter" ) ).toBeFalse();
			});

			it( title="the core bundle contains no org/objectweb classes", body=function( currentSpec ) {
				var entries = variables.core.findEntries( "org/objectweb", "*", true );
				expect( isNull( entries ) || !entries.hasMoreElements() ).toBeTrue();
				var shaded = variables.core.findEntries( "lucee/shaded/asm", "*.class", true );
				var count = 0;
				while ( shaded.hasMoreElements() ) {
					shaded.nextElement();
					count++;
				}
				expect( count ).toBeGT( 100 );
			});

			it( title="the core does not require the ASM bundles anymore", body=function( currentSpec ) {
				var requires = variables.core.getHeaders().get( "Require-Bundle" );
				expect( requires ).notToInclude( "objectweb" );
				var imports = variables.core.getHeaders().get( "Import-Package" ) ?: "";
				expect( imports ).notToInclude( "objectweb" );
			});

			it( title="the license of ASM is in the core", body=function( currentSpec ) {
				var entry = variables.core.getEntry( "META-INF/licenses/org.ow2.asm/LICENSE" );
				expect( isNull( entry ) ).toBeFalse();
				var license = toString( createObject( "java", "lucee.commons.io.IOUtil" ).toBytes( entry.openStream() ), "utf-8" );
				expect( license ).toInclude( "ASM: a very small and fast Java bytecode manipulation framework" );
			});

			it( title="the compiler works, an inline component is compiled and executed with the shaded ASM", body=function( currentSpec ) {
				var cmp = new component {
					function answer( numeric a, numeric b ) {
						return arguments.a + arguments.b;
					}
				};
				expect( cmp.answer( 20, 22 ) ).toBe( 42 );
			});
		});
	}
}
