component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults, testBox ) {
		describe( "LDEV-6990: the engine must be shut down in CFMLServlet.destroy()", function() {

			it( "the jakarta CFMLServlet declares destroy() to shut down the engine", function() {
				expect( declaresDestroy( "lucee.loader.servlet.jakarta.CFMLServlet", true ) ).toBeTrue( "lucee.loader.servlet.jakarta.CFMLServlet does not declare destroy()" );
			});

			it( "the javax CFMLServlet declares destroy() to shut down the engine", function() {
				// the javax servlet API is not necessarily on the classpath, skip the check if it is not
				expect( declaresDestroy( "lucee.loader.servlet.javax.CFMLServlet", false ) ).toBeTrue( "lucee.loader.servlet.javax.CFMLServlet does not declare destroy()" );
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
}
