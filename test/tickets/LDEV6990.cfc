component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults, testBox ) {
		describe( "LDEV-6990: Lucee and Felix must not keep the JVM alive on shutdown", function() {

			it( "the jakarta CFMLServlet declares destroy() to shut down the engine", function() {
				expect( declaresDestroy( "lucee.loader.servlet.jakarta.CFMLServlet", true ) ).toBeTrue( "lucee.loader.servlet.jakarta.CFMLServlet does not declare destroy()" );
			});

			it( "the javax CFMLServlet declares destroy() to shut down the engine", function() {
				// the javax servlet API is not necessarily on the classpath, skip the check if it is not
				expect( declaresDestroy( "lucee.loader.servlet.javax.CFMLServlet", false ) ).toBeTrue( "lucee.loader.servlet.javax.CFMLServlet does not declare destroy()" );
			});

			it( "the FelixDispatchQueue thread is a daemon thread", function() {
				var threads = getFelixThreads( "FelixDispatchQueue" );
				expect( arrayLen( threads ) ).toBeGT( 0, "no FelixDispatchQueue thread found" );
				loop array=threads item="local.t" {
					expect( t.isDaemon() ).toBeTrue( "thread [#t.getName()#] (id #t.getId()#) is not a daemon thread" );
				}
			});

			it( "no Felix thread is a non daemon thread", function() {
				loop array=getFelixThreads( "Felix" ) item="local.t" {
					// FelixShutdown is short lived and inherits the daemon flag from the thread stopping the framework
					if ( left( t.getName(), 13 ) == "FelixShutdown" ) continue;
					expect( t.isDaemon() ).toBeTrue( "thread [#t.getName()#] (id #t.getId()#) is not a daemon thread" );
				}
			});

		});
	}

	private boolean function declaresDestroy( required string className, required boolean servletApiRequired ) {
		var cl = createObject( "java", "lucee.loader.engine.CFMLEngineFactory" ).getInstance().getCFMLEngineFactory().getClass().getClassLoader();
		try {
			var methods = cl.loadClass( arguments.className ).getDeclaredMethods();
		}
		catch ( any e ) {
			if ( !arguments.servletApiRequired && findNoCase( "NoClassDefFound", e.type & " " & e.message ) ) return true;
			rethrow;
		}
		loop array=methods item="local.m" {
			if ( m.getName() == "destroy" && arrayLen( m.getParameterTypes() ) == 0 ) return true;
		}
		return false;
	}

	private array function getFelixThreads( required string prefix ) {
		var result = [];
		var it = createObject( "java", "java.lang.Thread" ).getAllStackTraces().keySet().iterator();
		while ( it.hasNext() ) {
			var t = it.next();
			if ( t.isAlive() && left( t.getName(), len( arguments.prefix ) ) == arguments.prefix ) arrayAppend( result, t );
		}
		return result;
	}
}
