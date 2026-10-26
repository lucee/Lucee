/**
 * Lucee is one jar, the core is not an OSGi bundle anymore. This gives the tests access to the Lucee jar
 * with the same methods the OSGi Bundle had (loadClass, getHeaders, findEntries, getEntry).
 */
component {

	function init() {
		variables.cl = createObject( "java", "lucee.runtime.osgi.OSGiUtil" ).getClass().getClassLoader();
		// the build with a separate core (core.lco loaded as a bundle) is still around, then the bundle is the core
		variables.bundle = "";
		try {
			variables.bundle = variables.cl.getBundle();
		}
		catch ( any e ) {
			variables.bundle = "";
		}
		var location = createObject( "java", "lucee.runtime.osgi.OSGiUtil" ).getClass().getProtectionDomain().getCodeSource().getLocation().toString();
		variables.jar = location.endsWith( ".jar" ) ? createObject( "java", "java.net.URI" ).init( location ).getPath() : "";
		return this;
	}

	function loadClass( required string className ) {
		if ( !isSimpleValue( variables.bundle ) ) return variables.bundle.loadClass( arguments.className );
		return createObject( "java", "java.lang.Class" ).forName( arguments.className, false, variables.cl );
	}

	function getEntry( required string path ) {
		if ( !isSimpleValue( variables.bundle ) ) return variables.bundle.getEntry( arguments.path );
		return variables.cl.getResource( arguments.path );
	}

	/** the headers of the manifest of Lucee (the one with the Bundle-SymbolicName lucee.core) */
	function getHeaders() {
		if ( !isSimpleValue( variables.bundle ) ) return variables.bundle.getHeaders();
		var headers = createObject( "java", "java.util.Hashtable" ).init();
		var urls = variables.cl.getResources( "META-INF/MANIFEST.MF" );
		while ( urls.hasMoreElements() ) {
			var mani = createObject( "java", "java.util.jar.Manifest" ).init( urls.nextElement().openStream() );
			var attrs = mani.getMainAttributes();
			if ( isNull( attrs.getValue( "Bundle-SymbolicName" ) ) || attrs.getValue( "Bundle-SymbolicName" ) != "lucee.core" ) continue;
			var it = attrs.entrySet().iterator();
			while ( it.hasNext() ) {
				var e = it.next();
				headers.put( e.getKey().toString(), e.getValue().toString() );
			}
			return headers;
		}
		throw "the manifest of Lucee was not found";
	}

	/** all entries of the Lucee jar below the path, pattern is "*" or "*.class" */
	function findEntries( required string path, required string pattern, boolean recurse = true ) {
		if ( !isSimpleValue( variables.bundle ) ) return variables.bundle.findEntries( arguments.path, arguments.pattern, arguments.recurse );
		var result = createObject( "java", "java.util.Vector" ).init();
		if ( !len( variables.jar ) ) return result.elements();
		var jf = createObject( "java", "java.util.jar.JarFile" ).init( variables.jar );
		try {
			var entries = jf.entries();
			var prefix = arguments.path & "/";
			var suffix = arguments.pattern == "*" ? "" : replace( arguments.pattern, "*", "" );
			while ( entries.hasMoreElements() ) {
				var name = entries.nextElement().getName();
				if ( left( name, len( prefix ) ) != prefix || ( len( suffix ) && right( name, len( suffix ) ) != suffix ) ) continue;
				if ( right( name, 1 ) == "/" ) continue;
				result.add( variables.cl.getResource( name ) );
			}
		}
		finally {
			jf.close();
		}
		return result.elements();
	}
}
