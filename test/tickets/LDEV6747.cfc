component extends="org.lucee.cfml.test.LuceeTestCase" labels="maven" {

	function beforeAll() {
		variables.repo = getTempDirectory() & "LDEV6747-" & createUUID() & "/";
		variables.group = "org.lucee.test.ldev6747.g" & lCase( replace( createUUID(), "-", "", "all" ) );
		directoryCreate( repo, true );

		// root
		//   a:1.0
		//     c:2.0           <- nearest, wins
		//     g:1.0           <- same depth as g:2.0, declared first, wins
		//   b:1.0
		//     d:1.0
		//       c:1.0         <- deeper, loses (and its dependency f is not followed)
		//         f:1.0
		//     g:2.0
		artifact( "root", "1.0", [ "a:1.0", "b:1.0" ] );
		artifact( "a", "1.0", [ "c:2.0", "g:1.0" ] );
		artifact( "b", "1.0", [ "d:1.0", "g:2.0" ] );
		artifact( "d", "1.0", [ "c:1.0" ] );
		artifact( "c", "2.0" );
		artifact( "c", "1.0", [ "f:1.0" ] );
		artifact( "f", "1.0" );
		artifact( "g", "1.0" );
		artifact( "g", "2.0" );
	}

	function afterAll() {
		if ( directoryExists( variables.repo ) ) directoryDelete( variables.repo, true );
	}

	function run( testResults, testBox ) {
		describe( "LDEV-6747: Maven dependency mediation (nearest wins)", function() {

			it( "getJarPOMs() returns only one version per groupId:artifactId", function() {
				var jars = getJarPOMs();
				expect( structKeyList( jars ) ).toBe( listSort( "root,a,b,c,d,g", "text" ) );
				loop collection=jars key="local.artifactId" value="local.versions" {
					expect( arrayLen( versions ) ).toBe( 1, "artifact [#artifactId#] in versions [#arrayToList( versions )#]" );
				}
			});

			it( "the version nearest to the root wins", function() {
				expect( arrayToList( getJarPOMs().c ) ).toBe( "2.0" );
			});

			it( "on the same depth the version declared first wins", function() {
				expect( arrayToList( getJarPOMs().g ) ).toBe( "1.0" );
			});

			it( "dependencies of a version that lost are not included", function() {
				expect( structKeyExists( getJarPOMs(), "f" ) ).toBeFalse();
			});

		});
	}

	// artifactId -> array of versions returned by POM.getJarPOMs()
	private struct function getJarPOMs() {
		var POM = createObject( "java", "lucee.runtime.mvn.POM" );
		var dir = createObject( "java", "lucee.commons.io.res.ResourcesImpl" ).getFileResourceProvider().getResource( variables.repo );
		var root = POM.getInstance( dir, variables.group, "root", "1.0", nullValue() );
		var result = [:];
		loop array=root.getJarPOMs( false ).toArray() item="local.p" {
			expect( p.getGroupId() ).toBe( variables.group );
			if ( !structKeyExists( result, p.getArtifactId() ) ) result[ p.getArtifactId() ] = [];
			arrayAppend( result[ p.getArtifactId() ], p.getVersion() );
		}
		var sorted = [:];
		loop array=listToArray( listSort( structKeyList( result ), "text" ) ) item="local.k" {
			sorted[ k ] = result[ k ];
		}
		return sorted;
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
