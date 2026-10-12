component extends="org.lucee.cfml.test.LuceeTestCase" labels="mvn" {

	function beforeAll() {
		variables.mvnDir = getTempDirectory() & "ldev6555-" & createUUID() & "/";
		directoryCreate( variables.mvnDir );
	}

	function afterAll() {
		if ( directoryExists( variables.mvnDir ) ) directoryDelete( variables.mvnDir, true );
	}

	function run( testResults, testBox ) {
		describe( "LDEV-6555 Maven classifier support in GAVSO", function() {

			it( title="string g:a", body=function() {
				var g = parse( "org.lucee:image-extension" );
				expect( g.g ).toBe( "org.lucee" );
				expect( g.a ).toBe( "image-extension" );
				expect( isNull( g.v ) ).toBeTrue();
				expect( isNull( g.classifier ) ).toBeTrue();
			});

			it( title="string g:a:v has no classifier", body=function() {
				var g = parse( "org.lucee:image-extension:2.0.0.35" );
				expect( g.v ).toBe( "2.0.0.35" );
				expect( isNull( g.classifier ) ).toBeTrue();
				expect( g.toString() ).toBe( "org.lucee:image-extension:2.0.0.35" );
			});

			it( title="string g:a:v:classifier", body=function() {
				var g = parse( "org.lucee:image-extension:2.0.0.35:lite" );
				expect( g.v ).toBe( "2.0.0.35" );
				expect( g.classifier ).toBe( "lite" );
				expect( isNull( g.extension ) ).toBeTrue();
				expect( g.toGAV() ).toBe( "org.lucee:image-extension:2.0.0.35" );
				expect( g.toGAVC() ).toBe( "org.lucee:image-extension:2.0.0.35:lite" );
			});

			it( title="a scope word in 4th position is a classifier", body=function() {
				var g = parse( "org.lucee:image-extension:2.0.0.35:runtime" );
				expect( g.classifier ).toBe( "runtime" );
				expect( isNull( g.s ) ).toBeTrue();
			});

			it( title="string g:a:v:classifier@ext", body=function() {
				var g = parse( "org.lucee:image-extension:2.0.0.35:lite@lex" );
				expect( g.classifier ).toBe( "lite" );
				expect( g.extension ).toBe( "lex" );
				expect( g.toString() ).toBe( "org.lucee:image-extension:2.0.0.35:lite@lex" );
			});

			it( title="string g:a:v@ext", body=function() {
				var g = parse( "org.lucee:image-extension:2.0.0.35@lex" );
				expect( isNull( g.classifier ) ).toBeTrue();
				expect( g.extension ).toBe( "lex" );
			});

			it( title="struct long keys", body=function() {
				var g = parse( { groupId: "org.lucee", artifactId: "image-extension", version: "2.0.0.35", classifier: "lite" } );
				expect( g.classifier ).toBe( "lite" );
				expect( g.toGAVC() ).toBe( "org.lucee:image-extension:2.0.0.35:lite" );
			});

			it( title="struct short keys", body=function() {
				var g = parse( { g: "org.lucee", a: "image-extension", v: "2.0.0.35", classifier: "lite" } );
				expect( g.toGAVC() ).toBe( "org.lucee:image-extension:2.0.0.35:lite" );
			});

			it( title="struct keeps scope, optional and checksum", body=function() {
				var g = parse( { groupId: "a", artifactId: "b", version: "1", scope: "runtime", optional: "true", checksum: "sha1:abc" } );
				expect( g.s ).toBe( "runtime" );
				expect( g.o ).toBe( "true" );
				expect( g.checksum ).toBe( "sha1:abc" );
			});

			it( title="struct maven key", body=function() {
				var g = parse( { maven: "org.lucee:image-extension:2.0.0.35:lite" } );
				expect( g.classifier ).toBe( "lite" );
			});

			it( title="round trip", body=function() {
				var g1 = parse( "org.lucee:image-extension:2.0.0.35:lite@lex" );
				var g2 = parse( g1.toString() );
				expect( g2.toString() ).toBe( g1.toString() );
				expect( g2.equals( g1 ) ).toBeTrue();
			});

			it( title="equality and hashCode distinguish the classifier", body=function() {
				var plain = parse( "org.lucee:image-extension:2.0.0.35" );
				var lite = parse( "org.lucee:image-extension:2.0.0.35:lite" );
				var lite2 = parse( "org.lucee:image-extension:2.0.0.35:lite" );
				expect( plain.equals( lite ) ).toBeFalse();
				expect( lite.equals( lite2 ) ).toBeTrue();
				expect( lite.hashCode() ).toBe( lite2.hashCode() );
				expect( plain.same( lite ) ).toBeTrue();
			});

			it( title="populate writes the classifier to a struct", body=function() {
				var g = parse( "org.lucee:image-extension:2.0.0.35:lite" );
				var sct = createObject( "java", "lucee.runtime.mvn.MavenUtil$GAVSO" ).toStruct( g );
				expect( sct.classifier ).toBe( "lite" );
				expect( sct.version ).toBe( "2.0.0.35" );
			});

			it( title="too many parts throws", body=function() {
				expect( function() {
					parse( "a:b:c:d:e" );
				}).toThrow();
			});

			it( title="invalid input throws", body=function() {
				expect( function() {
					parse( "justone" );
				}).toThrow();
			});

		});

		describe( "LDEV-6555 Maven classifier download and local files", function() {

			it( title="release: classified artifact, POM stays unclassified", body=function() {
				var pom = pomOf( "org.lucee", "image-extension", "3.1.0.11", "lite" );
				var lex = pom.getArtifact( "lex" );
				expect( lex.getName() ).toBe( "image-extension-3.1.0.11-lite.lex" );
				expect( lex.isFile() ).toBeTrue();
				expect( pom.getPath().getName() ).toBe( "image-extension-3.1.0.11.pom" );
				expect( pom.getPath().isFile() ).toBeTrue();
				expect( fileExists( lex.getParent() & "/image-extension-3.1.0.11-lite.pom" ) ).toBeFalse();
			});

			it( title="release: classified and unclassified are different files", body=function() {
				var lite = pomOf( "org.lucee", "image-extension", "3.1.0.11", "lite" ).getArtifact( "lex" );
				var full = pomOf( "org.lucee", "image-extension", "3.1.0.11", "" ).getArtifact( "lex" );
				expect( full.getName() ).toBe( "image-extension-3.1.0.11.lex" );
				expect( full.isFile() ).toBeTrue();
				expect( lite.length() ).notToBe( full.length() );
			});

			it( title="snapshot: the metadata entry with the matching classifier is used", body=function() {
				var lite = pomOf( "org.lucee", "image-extension", "3.1.0.11-SNAPSHOT", "lite" ).getArtifact( "lex" );
				expect( lite.getName() ).toBe( "image-extension-3.1.0.11-SNAPSHOT-lite.lex" );
				expect( lite.isFile() ).toBeTrue();
				var full = pomOf( "org.lucee", "image-extension", "3.1.0.11-SNAPSHOT", "" ).getArtifact( "lex" );
				expect( full.getName() ).toBe( "image-extension-3.1.0.11-SNAPSHOT.lex" );
				expect( lite.length() ).notToBe( full.length() );
			});

			it( title="classified jar from Central", body=function() {
				var jar = pomOf( "org.lwjgl", "lwjgl", "3.3.4", "natives-linux" ).getArtifact( "jar" );
				expect( jar.getName() ).toBe( "lwjgl-3.3.4-natives-linux.jar" );
				expect( jar.isFile() ).toBeTrue();
			});

			it( title="a dependency with a classifier resolves to the classified jar", body=function() {
				var dir = variables.mvnDir & "dep/";
				var pomDir = dir & "test/ldev6555/1.0/";
				directoryCreate( pomDir );
				fileWrite( pomDir & "ldev6555-1.0.pom", '<?xml version="1.0"?>
<project xmlns="http://maven.apache.org/POM/4.0.0">
	<modelVersion>4.0.0</modelVersion>
	<groupId>test</groupId>
	<artifactId>ldev6555</artifactId>
	<version>1.0</version>
	<packaging>pom</packaging>
	<dependencies>
		<dependency>
			<groupId>org.lwjgl</groupId>
			<artifactId>lwjgl</artifactId>
			<version>3.3.4</version>
			<classifier>natives-linux</classifier>
		</dependency>
	</dependencies>
</project>' );
				var pom = createObject( "java", "lucee.runtime.mvn.POM" ).getInstance( toRes( dir ), "test", "ldev6555", "1.0", 9, javaCast( "null", "" ) );
				var names = [];
				for ( var jar in pom.getJars() ) arrayAppend( names, jar.getName() );
				expect( names ).toBe( [ "lwjgl-3.3.4-natives-linux.jar" ] );
			});

			it( title="mavenLoad with a classifier", body=function() {
				var res = mavenLoad( "org.lwjgl:lwjgl:3.3.4:natives-linux" );
				var found = false;
				for ( var path in res ) if ( listLast( path, "/\\" ) == "lwjgl-3.3.4-natives-linux.jar" ) found = true;
				expect( found ).toBeTrue();
			});

		});
	}

	private function toRes( required string path ) {
		return createObject( "java", "lucee.commons.io.res.util.ResourceUtil" ).toResourceNotExisting( getPageContext(), arguments.path );
	}

	private function pomOf( required string g, required string a, required string v, required string classifier ) {
		var dir = toRes( variables.mvnDir & "repo/" );
		dir.mkdirs();
		return createObject( "java", "lucee.runtime.mvn.POM" ).getInstance( dir, g, a, v, classifier, 9, javaCast( "null", "" ) );
	}

	private function parse( required any input ) {
		return createObject( "java", "lucee.runtime.mvn.MavenUtil" ).toGAVSO( arguments.input );
	}

}
