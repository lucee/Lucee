// creates the extensions (.lex) and bundles the LDEV-6829 tests use
component {

	public function createJar( required boolean bundle ) {
		var bos = createObject( "java", "java.io.ByteArrayOutputStream" ).init();
		var zos = createObject( "java", "java.util.zip.ZipOutputStream" ).init( bos );
		var mf = "Manifest-Version: 1.0#chr( 10 )#";
		if ( arguments.bundle ) mf &= "Bundle-ManifestVersion: 2#chr( 10 )#Bundle-SymbolicName: ldev6829.stub#chr( 10 )#Bundle-Version: 1.0.0#chr( 10 )#";
		mf &= chr( 10 );
		addEntry( zos, "META-INF/MANIFEST.MF", mf.getBytes( "UTF-8" ) );
		zos.close();
		return bos.toByteArray();
	}

	public function createLex( required string path, required string groupId, required string artifactId, required string name, required string id, required string version, required struct attrs, required struct files ) {
		var mf = 'Manifest-Version: 1.0#chr( 10 )#groupId: "#arguments.groupId#"#chr( 10 )#artifactId: "#arguments.artifactId#"#chr( 10 )#id: "#arguments.id#"#chr( 10 )#version: "#arguments.version#"#chr( 10 )#name: "#arguments.name#"#chr( 10 )#release-type: server#chr( 10 )#';
		loop struct=arguments.attrs index="local.k" item="local.v" {
			mf &= '#k#: "#v#"#chr( 10 )#';
		}
		mf &= chr( 10 );
		var dir = getDirectoryFromPath( arguments.path );
		if ( !directoryExists( dir ) ) directoryCreate( dir, true );
		var zos = createObject( "java", "java.util.zip.ZipOutputStream" ).init( createObject( "java", "java.io.FileOutputStream" ).init( arguments.path ) );
		try {
			addEntry( zos, "META-INF/MANIFEST.MF", mf.getBytes( "UTF-8" ) );
			loop struct=arguments.files index="local.entry" item="local.bytes" {
				addEntry( zos, entry, bytes );
			}
		}
		finally {
			zos.close();
		}
	}

	public function addEntry( required any zos, required string name, required any bytes ) {
		arguments.zos.putNextEntry( createObject( "java", "java.util.zip.ZipEntry" ).init( arguments.name ) );
		arguments.zos.write( arguments.bytes );
		arguments.zos.closeEntry();
	}

}
