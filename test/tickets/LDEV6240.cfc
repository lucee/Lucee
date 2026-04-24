component extends="org.lucee.cfml.test.LuceeTestCase" labels="java,classloader" {

	function beforeAll() {
		variables.tempDir = getTempDirectory() & "LDEV6240/";
		if ( directoryExists( variables.tempDir ) ) directoryDelete( variables.tempDir, true );
		directoryCreate( variables.tempDir );
		directoryCreate( variables.tempDir & "src/" );
		directoryCreate( variables.tempDir & "classes1/" );
		directoryCreate( variables.tempDir & "classes2/" );

		// simple class with an interface, simulates an SPI provider
		var interfaceSrc = 'package ldev6240;
public interface Greeter {
	String greet();
}';
		var implSrc = 'package ldev6240;
public class HelloGreeter implements Greeter {
	public String greet() { return "hello"; }
}';
		// a consumer that takes the interface as a method arg
		var consumerSrc = 'package ldev6240;
public class GreeterConsumer {
	public static String consume( Greeter g ) { return g.greet(); }
}';

		directoryCreate( variables.tempDir & "src/ldev6240/" );
		fileWrite( variables.tempDir & "src/ldev6240/Greeter.java", interfaceSrc );
		fileWrite( variables.tempDir & "src/ldev6240/HelloGreeter.java", implSrc );
		fileWrite( variables.tempDir & "src/ldev6240/GreeterConsumer.java", consumerSrc );

		var javac = createObject( "java", "javax.tools.ToolProvider" ).getSystemJavaCompiler();
		if ( isNull( javac ) )
			throw( message="JDK required - javac not available", type="application" );

		var result = javac.run(
			javaCast( "null", "" ), javaCast( "null", "" ), javaCast( "null", "" ),
			[
				"-d", variables.tempDir & "classes1",
				variables.tempDir & "src/ldev6240/Greeter.java",
				variables.tempDir & "src/ldev6240/HelloGreeter.java",
				variables.tempDir & "src/ldev6240/GreeterConsumer.java"
			]
		);
		if ( result != 0 )
			throw( message="javac compilation failed with exit code #result#", type="application" );

		// copy to classes2 - same bytes, different classloader will make them different types
		directoryCreate( variables.tempDir & "classes2/ldev6240/" );
		for ( var f in [ "Greeter.class", "HelloGreeter.class", "GreeterConsumer.class" ] ) {
			fileCopy(
				variables.tempDir & "classes1/ldev6240/" & f,
				variables.tempDir & "classes2/ldev6240/" & f
			);
		}
	}

	private function createIsolatedClassLoader( required string classDir ) {
		var file = createObject( "java", "java.io.File" ).init( arguments.classDir );
		var urlArray = createObject( "java", "java.lang.reflect.Array" )
			.newInstance( createObject( "java", "java.net.URL" ).getClass(), 1 );
		urlArray[ 1 ] = file.toURI().toURL();
		// null parent forces independent loading - each CL gets its own copy of every class
		return createObject( "java", "java.net.URLClassLoader" )
			.init( urlArray, javaCast( "null", "" ) );
	}

	private function getEnvClassLoader() {
		return createObject( "java", "lucee.runtime.osgi.EnvClassLoader" )
			.getInstance( javaCast( "null", "" ) );
	}

	private function getCallerCache( required envCL ) {
		var field = arguments.envCL.getClass().getDeclaredField( "callerCache" );
		field.setAccessible( true );
		return field.get( arguments.envCL );
	}

	function run( testResults, testBox ) {
		describe( "LDEV-6240: EnvClassLoader stale class resolution", function() {

			it( "findLoadedClass prevents EnvClassLoader from seeing classloader changes", function() {
				// This is the core bug. EnvClassLoader.loadClass() calls findLoadedClass()
				// which is a JVM-level cache that cannot be cleared. Once a class is loaded
				// through EnvClassLoader, it's stuck there forever, even if the underlying
				// classloader that provided it has been flushed and replaced.
				//
				// We can't test this directly on the live EnvClassLoader (it would pollute
				// the running server's state), but we can demonstrate the JVM behaviour
				// with a fresh URLClassLoader that mimics EnvClassLoader's pattern.

				var cl1 = createIsolatedClassLoader( variables.tempDir & "classes1/" );
				var cl2 = createIsolatedClassLoader( variables.tempDir & "classes2/" );
				try {
					// create a wrapper classloader that delegates to cl1 (simulates EnvClassLoader)
					var wrapperClass = createObject( "java", "java.net.URLClassLoader" );
					var emptyUrls = createObject( "java", "java.lang.reflect.Array" )
						.newInstance( createObject( "java", "java.net.URL" ).getClass(), 0 );
					var wrapper = wrapperClass.init( emptyUrls, cl1 );

					// first load: goes through cl1
					var class1 = wrapper.loadClass( "ldev6240.Greeter" );
					expect( class1.getName() ).toBe( "ldev6240.Greeter" );

					// load same class directly from cl2
					var class2 = cl2.loadClass( "ldev6240.Greeter" );

					// same name, different classloader, different identity
					expect( class1.getName() ).toBe( class2.getName() );
					expect( class1.isAssignableFrom( class2 ) ).toBeFalse(
						"classes from different classloaders should not be assignable"
					);

					// wrapper.loadClass() will return the SAME class1 due to findLoadedClass
					// even though we might want it to return class2 after a "flush"
					var classAgain = wrapper.loadClass( "ldev6240.Greeter" );
					expect( classAgain.equals( class1 ) ).toBeTrue(
						"findLoadedClass returns the original - JVM cache can't be cleared"
					);
				}
				finally {
					cl1.close();
					cl2.close();
				}
			});

			it( "cross-classloader objects fail isAssignableFrom even with identical bytes", function() {
				// Fundamental JVM behaviour: class identity = name + classloader.
				// Same .class bytes from different classloaders are NOT the same type.
				var cl1 = createIsolatedClassLoader( variables.tempDir & "classes1/" );
				var cl2 = createIsolatedClassLoader( variables.tempDir & "classes1/" ); // SAME dir
				try {
					var iface1 = cl1.loadClass( "ldev6240.Greeter" );
					var iface2 = cl2.loadClass( "ldev6240.Greeter" );

					expect( iface1.getName() ).toBe( iface2.getName() );
					expect( iface1.getClassLoader().equals( iface2.getClassLoader() ) ).toBeFalse();
					expect( iface1.isAssignableFrom( iface2 ) ).toBeFalse(
						"same bytes, different classloaders = different types"
					);

					// impl from cl2 does NOT implement interface from cl1
					var impl2 = cl2.loadClass( "ldev6240.HelloGreeter" );
					expect( iface1.isAssignableFrom( impl2 ) ).toBeFalse(
						"impl from cl2 is not a subtype of interface from cl1"
					);

					// but Lucee's name-based matching handles this (LDEV-6226 fix)
					var Reflector = createObject( "java", "lucee.runtime.reflection.Reflector" );
					expect( Reflector.isInstaneOf( impl2, iface1, false ) ).toBeTrue(
						"name-based isInstaneOf works across classloaders"
					);
				}
				finally {
					cl1.close();
					cl2.close();
				}
			});

			it( "EnvClassLoader callerCache is populated and queryable", function() {
				// Verify callerCache exists and is used. This cache is the secondary
				// staleness vector (after findLoadedClass). Unlike findLoadedClass,
				// callerCache CAN be cleared - that's the fix for LDEV-6240.
				var envCL = getEnvClassLoader();
				var callerCache = getCallerCache( envCL );

				// load something to ensure cache is populated
				envCL.loadClass( "java.util.HashMap" );

				// callerCache should be non-empty (many classes loaded by now)
				expect( callerCache.size() ).toBeGT( 0,
					"callerCache should have entries after class loading"
				);
			});

			it( "EnvClassLoader has no cache invalidation API", function() {
				// Documents the gap: there is no way to clear EnvClassLoader's caches.
				// After LDEV-6240 fix, update this test to verify clearCallerCache() works.
				var envCL = getEnvClassLoader();

				var hasClearMethod = false;
				try {
					envCL.getClass().getMethod( "clearCallerCache", [] );
					hasClearMethod = true;
				}
				catch ( java.lang.NoSuchMethodException e ) {
					// expected pre-fix
				}

				if ( !hasClearMethod ) {
					// pre-fix: document the gap
					var callerCache = getCallerCache( envCL );
					var sizeBefore = callerCache.size();
					expect( sizeBefore ).toBeGT( 0,
						"callerCache has entries but no API to clear them"
					);
				}
				else {
					// post-fix: verify it actually works
					var callerCache = getCallerCache( envCL );
					envCL.loadClass( "java.util.TreeMap" ); // ensure something is cached
					expect( callerCache.size() ).toBeGT( 0 );

					envCL.clearCallerCache();

					expect( callerCache.size() ).toBe( 0,
						"clearCallerCache() should empty the cache"
					);

					// verify classes still resolve after clearing (re-populates cache)
					var cls = envCL.loadClass( "java.util.TreeMap" );
					expect( cls ).notToBeNull();
					expect( cls.getName() ).toBe( "java.util.TreeMap" );
				}
			});

			it( "simulates stale class after classloader swap", function() {
				// End-to-end simulation of the bug:
				// 1. CL-A loads Greeter interface and HelloGreeter impl
				// 2. CL-A is "flushed" (closed, replaced by CL-B with same classes)
				// 3. CL-B loads Greeter - this is a DIFFERENT Class object
				// 4. The old HelloGreeter (from CL-A) is NOT assignable to new Greeter (from CL-B)
				// 5. This is what causes ServiceLoader's "not a subtype" error

				var clA = createIsolatedClassLoader( variables.tempDir & "classes1/" );
				try {
					// step 1: load from CL-A (the "before flush" state)
					var greeterA = clA.loadClass( "ldev6240.Greeter" );
					var implA = clA.loadClass( "ldev6240.HelloGreeter" );
					var instanceA = implA.getConstructor( [] ).newInstance( [] );

					// works fine - same classloader
					expect( greeterA.isAssignableFrom( implA ) ).toBeTrue();
					expect( instanceA.greet() ).toBe( "hello" );
				}
				finally {
					clA.close(); // step 2: "flush" CL-A
				}

				// step 3: CL-B takes over (simulates new PhysicalClassLoader after flush)
				var clB = createIsolatedClassLoader( variables.tempDir & "classes1/" );
				try {
					var greeterB = clB.loadClass( "ldev6240.Greeter" );

					// step 4: the OLD instance from CL-A is not compatible with CL-B's type
					expect( greeterB.isAssignableFrom( instanceA.getClass() ) ).toBeFalse(
						"old instance's class is not assignable to new classloader's interface"
					);

					// step 5: this is what ServiceLoader sees - "not a subtype"
					// ServiceLoader.load(greeterB, envCL) → envCL returns old impl → boom

					// Lucee's name-based matching (LDEV-6226) handles this for
					// Reflector/Clazz code paths. The remaining gap is ServiceLoader
					// and other JDK code that uses isAssignableFrom directly.
					var Reflector = createObject( "java", "lucee.runtime.reflection.Reflector" );
					expect( Reflector.isInstaneOf( instanceA.getClass(), greeterB, false ) ).toBeTrue(
						"name-based matching works even after classloader swap"
					);
					// Reflector.like() cross-classloader support is tested in LDEV6226.cfc
				}
				finally {
					clB.close();
				}
			});

		});
	}
}