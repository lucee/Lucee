component extends="org.lucee.cfml.test.LuceeTestCase" labels="maven" {

	function beforeAll() {
		variables.repo = getTempDirectory() & "LDEV6946-" & createUUID() & "/";
		variables.group = "org.lucee.test.ldev6946.g" & lCase( replace( createUUID(), "-", "", "all" ) );
		directoryCreate( repo, true );

		// the mail extension case: org.lucee:mail declares jakarta.mail 2.0.2 itself and excludes the
		// jakarta.mail 2.0.1 that commons-email2-jakarta asks for, the .lex only contains 2.0.2.
		// root
		//   lib:1.0
		//     api:1.0         <- loses against api:2.0, NOT in the local repository and not downloadable
		//   api:2.0           <- nearest, wins
		artifact( "root", "1.0", [ "lib:1.0", "api:2.0" ] );
		artifact( "lib", "1.0", [ "api:1.0" ] );
		artifact( "api", "2.0" );
	}

	function afterAll() {
		if ( directoryExists( variables.repo ) ) directoryDelete( variables.repo, true );
	}

	function run( testResults, testBox ) {
		describe( "LDEV-6946: a dependency version that loses the mediation is not loaded", function() {

			it( "getJarPOMs() does not need the losing version (offline install of the mail extension)", function() {
				var POM = createObject( "java", "lucee.runtime.mvn.POM" );
				var dir = createObject( "java", "lucee.commons.io.res.ResourcesImpl" ).getFileResourceProvider().getResource( variables.repo );
				var root = POM.getInstance( dir, variables.group, "root", "1.0", nullValue() );
				var result = [];
				loop array=root.getJarPOMs( false ).toArray() item="local.p" {
					arrayAppend( result, p.getArtifactId() & ":" & p.getVersion() );
				}
				arraySort( result, "text" );
				expect( arrayToList( result ) ).toBe( "api:2.0,lib:1.0,root:1.0" );
			});

			it( "the losing version is not downloaded", function() {
				expect( directoryExists( variables.repo & replace( variables.group, ".", "/", "all" ) & "/api/1.0/" ) ).toBeFalse();
			});

		});
	}

	// writes a pom and a jar into the local repository
	private void function artifact( required string artifactId, required string version, array dependencies = [] ) {
		var dir = variables.repo & replace( variables.group, ".", "/", "all" ) & "/" & arguments.artifactId & "/" & arguments.version & "/";
		directoryCreate( dir, true );
		var deps = "";
		loop array=arguments.dependencies item="local.dep" {
			deps &= "<dependency><groupId>#variables.group#</groupId><artifactId>#listFirst( dep, ':' )#</artifactId><version>#listLast( dep, ':' )#</version></dependency>";
		}
		fileWrite( dir & arguments.artifactId & "-" & arguments.version & ".pom", '<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0">
	<modelVersion>4.0.0</modelVersion>
	<groupId>#variables.group#</groupId>
	<artifactId>#arguments.artifactId#</artifactId>
	<version>#arguments.version#</version>
	<packaging>jar</packaging>
	<dependencies>#deps#</dependencies>
</project>' );
		var zos = createObject( "java", "java.util.zip.ZipOutputStream" ).init( createObject( "java", "java.io.FileOutputStream" ).init( dir & arguments.artifactId & "-" & arguments.version & ".jar" ) );
		try {
			zos.putNextEntry( createObject( "java", "java.util.zip.ZipEntry" ).init( "META-INF/MANIFEST.MF" ) );
			zos.write( "Manifest-Version: 1.0#chr(10)#".getBytes( "UTF-8" ) );
			zos.closeEntry();
		}
		finally {
			zos.close();
		}
	}
}
