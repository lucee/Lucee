<cfscript>
	// LDEV-7012: runs in a new Lucee server (see ../LDEV7012.cfc), executes one scenario and writes the result to a json file
	System = createObject( "java", "java.lang.System" );
	scenario = System.getProperty( "ldev7012.scenario" );
	resultFile = System.getProperty( "ldev7012.result" );
	work = System.getProperty( "ldev7012.dir" );

	lex = new Lex();
	Installer = createObject( "java", "lucee.runtime.osgi.OSGiExtensionInstaller" );
	ConfigUtil = createObject( "java", "lucee.runtime.config.ConfigUtil" );
	RHExtension = createObject( "java", "lucee.runtime.extension.RHExtension" );
	DeployHandler = createObject( "java", "lucee.runtime.config.DeployHandler" );

	cs = ConfigUtil.getConfigServerImpl( getPageContext().getConfig() );
	serverContext = expandPath( "/WEB-INF/lucee-server/context/" );
	log = cs.getLog( "deploy" );

	result = { "scenario": scenario, "error": "", "installed": [] };

	try {
		// the extensions the scenarios use
		// the OSGi extension as .lex in the extension folder (as the bundled .lex files are), the test puts it there before the server starts, this is the offline source
		osgiLex = work & "osgi.lex";
		bundleLex = work & "bundle.lex";
		plainLex = work & "plain.lex";
		cacheLex = work & "cache.lex";
		classLex = work & "class.lex";
		directoryCreate( work, true, true );

		// the OSGi extension of the server that runs the test (the test copies it to the extension folder of this server)
		fileCopy( System.getProperty( "ldev7012.osgilexfile" ), osgiLex );

		// ships a bundle
		lex.createLex( bundleLex, "ldev7012", "bundle", "LDEV7012 bundle", createUUID(), "1.0.0.1", {}, { "jars/stub-bundle.jar": lex.createJar( true ) } );
		// ships a jar that is not a bundle
		lex.createLex( plainLex, "ldev7012", "plain", "LDEV7012 plain", createUUID(), "1.0.0.1", {}, { "jars/plain.jar": lex.createJar( false ) } );
		// defines a cache with a class from a bundle
		lex.createLex( cacheLex, "ldev7012", "cache", "LDEV7012 cache", createUUID(), "1.0.0.1", { "cache": "[{'class':'ldev7012.Cache','bundleName':'ldev7012.stub','bundleVersion':'1.0.0'}]" }, {} );
		// defines a datasource with a class from the classpath
		lex.createLex( classLex, "ldev7012", "class", "LDEV7012 class", createUUID(), "1.0.0.1", { "jdbc": '[{"label":"x","class":"ldev7012.Driver","connectionString":"jdbc:x"}]' }, {} );

		if ( scenario == "classify" ) {
			result.bundle = needs( bundleLex );
			result.plain = needs( plainLex );
			result.cache = needs( cacheLex );
			result.class = needs( classLex );
			result.osgi = needs( osgiLex );
			result.isOsgiExtension = isOsgiExtension( osgiLex );
			result.isOsgiExtensionBundle = isOsgiExtension( bundleLex );
			result.message = Installer.message( "the test", "detail" );
		}
		else {
			// this server has no OSGi extension (lucee.extensions.config.only)
			result.osgiAtStart = createObject( "java", "lucee.runtime.osgi.OSGi" ).isAvailable();
			// the jar can bundle the OSGi extension (it is extracted to the extension folder), the scenario has to start without it
			if ( scenario == "unresolved" ) {
				loop array=directoryList( serverContext & "extensions/available/", true, "path", "*osgi-extension*" ) item="f" {
					fileDelete( f );
				}
			}
			if ( scenario == "extension" || scenario == "optout" || scenario == "unresolved" || scenario == "plain" ) {
				deploy( scenario == "plain" ? plainLex : bundleLex );
			}
			else if ( scenario == "classdefinition" ) {
				def = createObject( "java", "lucee.transformer.library.ClassDefinitionImpl" ).init( "ldev7012.Stub", "ldev7012.stub", "1.0.0", javacast( "null", "" ) );
				def.getClazz( true );
			}
			else throw "unknown scenario [#scenario#]";
			result.osgiAtEnd = createObject( "java", "lucee.runtime.osgi.OSGi" ).isAvailable();
		}
	}
	catch ( any e ) {
		result.error = e.message;
		result.type = e.type;
		result.cause = isNull( e.cause ) || isSimpleValue( e.cause ) ? "" : ( e.cause.message ?: "" );
	}

	// what is installed at the end
	result.installed = installedList();
	fileWrite( resultFile, serializeJSON( result ) );
// the server of this scenario keeps running otherwise
createObject( "java", "java.lang.System" ).exit( 0 );

	function deploy( required string lex ) {
		DeployHandler.deployExtension( cs, cs.getResource( arguments.lex ), false, false, RHExtension.ACTION_COPY, log );
	}

	function needs( required string lex ) {
		return Installer.needsOSGi( RHExtension.getInstance( cs, cs.getResource( arguments.lex ), log ) );
	}

	function isOsgiExtension( required string lex ) {
		return Installer.isOSGiExtension( RHExtension.getInstance( cs, cs.getResource( arguments.lex ), log ) );
	}

	// artifactId of every installed extension, in the order they were installed
	function installedList() {
		var files = directoryList( serverContext & "extensions/installed/", false, "query", "*.lex", "datelastmodified" );
		var res = [];
		// the extensions the server installs by default are not of interest
		var ours = [ "osgi-extension", "bundle", "plain", "cache", "class" ];
		loop query=files {
			var manifest = fileRead( "zip://" & files.directory & "/" & files.name & "!/META-INF/MANIFEST.MF" );
			var m = reFind( 'artifactId: "([^"]*)"', manifest, 1, true );
			var artifactId = m.pos[ 1 ] > 0 ? mid( manifest, m.pos[ 2 ], m.len[ 2 ] ) : "";
			if ( arrayFind( ours, artifactId ) ) arrayAppend( res, { "artifactId": artifactId, "modified": files.dateLastModified.getTime() } );
		}
		arraySort( res, function( a, b ) { return a.modified - b.modified; } );
		return res;
	}

</cfscript>
