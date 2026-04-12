component extends="org.lucee.cfml.test.LuceeTestCase" labels="java,classloader" {

	function beforeAll() {
		variables.tempDir = getTempDirectory() & "LDEV6226/";
		if ( directoryExists( variables.tempDir ) ) directoryDelete( variables.tempDir, true );
		directoryCreate( variables.tempDir );
		directoryCreate( variables.tempDir & "src/" );
		directoryCreate( variables.tempDir & "classes1/" );
		directoryCreate( variables.tempDir & "classes2/" );

		var javaSource = 'public class LDEV6226Helper {
	private String value;
	public LDEV6226Helper() { this.value = "default"; }
	public LDEV6226Helper( String value ) { this.value = value; }
	public LDEV6226Helper( LDEV6226Helper other ) { this.value = other.getValue(); }
	public String getValue() { return this.value; }
	public String extractFrom( LDEV6226Helper other ) { return other.getValue(); }
	public String extractFrom( Object other ) { return "object:" + other.toString(); }
}';

		fileWrite( variables.tempDir & "src/LDEV6226Helper.java", javaSource );

		var javac = createObject( "java", "javax.tools.ToolProvider" ).getSystemJavaCompiler();
		if ( isNull( javac ) )
			throw( message="JDK required - javac not available", type="application" );

		var result = javac.run(
			javaCast( "null", "" ), javaCast( "null", "" ), javaCast( "null", "" ),
			[ "-d", variables.tempDir & "classes1", variables.tempDir & "src/LDEV6226Helper.java" ]
		);
		if ( result != 0 )
			throw( message="javac compilation failed with exit code #result#", type="application" );
		fileCopy(
			variables.tempDir & "classes1/LDEV6226Helper.class",
			variables.tempDir & "classes2/LDEV6226Helper.class"
		);
	}

	private function createFileClassLoader( required string classDir ) {
		var file = createObject( "java", "java.io.File" ).init( arguments.classDir );
		var urlArray = createObject( "java", "java.lang.reflect.Array" )
			.newInstance( createObject( "java", "java.net.URL" ).getClass(), 1 );
		urlArray[ 1 ] = file.toURI().toURL();
		// null parent classloader forces each instance to load its own copy of the class,
		// giving us two Class objects with the same FQN but different identity (simulates OSGi bundle refresh)
		return createObject( "java", "java.net.URLClassLoader" )
			.init( urlArray, javaCast( "null", "" ) );
	}

	private function getDynamicInvokerClazz( required clazz ) {
		var di = createObject( "java", "lucee.transformer.dynamic.DynamicInvoker" ).getExistingInstance();
		if ( isNull( di ) )
			throw( message="DynamicInvoker not initialised in this context", type="application" );
		return di.toClazz( arguments.clazz );
	}

	// helper: loads the class from both classloaders, runs the callback, closes classloaders
	private function withCrossClassLoaders( required function callback ) {
		var cl1 = createFileClassLoader( variables.tempDir & "classes1/" );
		var cl2 = createFileClassLoader( variables.tempDir & "classes2/" );
		try {
			var class1 = cl1.loadClass( "LDEV6226Helper" );
			var class2 = cl2.loadClass( "LDEV6226Helper" );
			arguments.callback( class1, class2 );
		}
		finally {
			cl1.close();
			cl2.close();
		}
	}

	function run( testResults, testBox ) {
		describe( "LDEV-6226 - cross-classloader identity mismatch", function() {

			it( "isAssignableFrom fails across classloaders but name-based matching works", function() {
				withCrossClassLoaders( function( class1, class2 ) {
					// same name, different classloaders
					expect( class1.getName() ).toBe( class2.getName() );
					expect( class1.getClassLoader().toString() )
						.notToBe( class2.getClassLoader().toString() );

					// strict identity check fails - root cause of the bug
					expect( class1.isAssignableFrom( class2 ) ).toBeFalse();

					// name-based check works
					var Reflector = createObject( "java", "lucee.runtime.reflection.Reflector" );
					expect( Reflector.isInstaneOf( class2, class1, false ) ).toBeTrue();
				});
			});

			it( "Clazz.getMethod resolves with cross-classloader arguments", function() {
				withCrossClassLoaders( function( class1, class2 ) {
					var foreignArg = class2.getConstructor([ createObject( "java", "java.lang.String" ).getClass() ])
						.newInstance([ "test-value" ]);

					// resolve method on class1 (old classloader) with arg from class2 (new classloader)
					var clazzz = getDynamicInvokerClazz( class1 );
					var method = clazzz.getMethod( "extractFrom", [ foreignArg ], true, true, true );
					expect( method ).notToBeNull();
					expect( method.getName() ).toBe( "extractFrom" );
				});
			});

			it( "Clazz.getMethod picks the typed overload over Object for cross-classloader args", function() {
				withCrossClassLoaders( function( class1, class2 ) {
					var foreignArg = class2.getConstructor([ createObject( "java", "java.lang.String" ).getClass() ])
						.newInstance([ "overload-test" ]);

					// with overloaded extractFrom(LDEV6226Helper) and extractFrom(Object),
					// should resolve the more specific typed overload, not fall back to Object
					var clazzz = getDynamicInvokerClazz( class1 );
					var method = clazzz.getMethod( "extractFrom", [ foreignArg ], true, true, true );
					expect( method ).notToBeNull();

					// verify it picked the typed overload by checking param type
					var paramTypes = method.getArgumentClasses();
					expect( paramTypes[ 1 ].getName() ).toBe( "LDEV6226Helper",
						"should pick extractFrom(LDEV6226Helper) not extractFrom(Object)" );
				});
			});

			it( "Clazz.getConstructor resolves with cross-classloader arguments", function() {
				withCrossClassLoaders( function( class1, class2 ) {
					var foreignArg = class2.getConstructor([ createObject( "java", "java.lang.String" ).getClass() ])
						.newInstance([ "ctor-test" ]);

					var clazzz = getDynamicInvokerClazz( class1 );
					var ctor = clazzz.getConstructor( [ foreignArg ], true, true, javaCast( "null", "" ) );
					expect( ctor ).notToBeNull();
				});
			});

			it( "Reflector.like matches cross-classloader types", function() {
				withCrossClassLoaders( function( class1, class2 ) {
					var Reflector = createObject( "java", "lucee.runtime.reflection.Reflector" );
					expect( Reflector.like( class2, class1 ) ).toBeTrue(
						"Reflector.like() should match same class from different classloaders"
					);
				});
			});

			it( "Reflector.convert handles cross-classloader types", function() {
				withCrossClassLoaders( function( class1, class2 ) {
					var obj = class2.getConstructor([ createObject( "java", "java.lang.String" ).getClass() ])
						.newInstance([ "convert-test" ]);

					// convert() should recognise obj as compatible with class1's type
					var Reflector = createObject( "java", "lucee.runtime.reflection.Reflector" );
					var result = Reflector.convert( obj, class1, javaCast( "null", "" ) );
					expect( result ).notToBeNull();
				});
			});

		});
	}

}
