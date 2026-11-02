component extends="org.lucee.cfml.test.LuceeTestCase" labels="maven" {

	function beforeAll() {
		variables.repo = getTempDirectory() & "LDEV6187-" & createUUID() & "/";
		variables.group = "org.lucee.test.ldev6187.g" & lCase( replace( createUUID(), "-", "", "all" ) );
		directoryCreate( variables.repo, true );

		// parent has properties and dependencyManagement, child inherits both, so resolving the child
		// goes through initParent(), initProperties(), initDependencyManagement() and initDependencies()
		artifact( "parent", "1.0", [], [ "lib:${lib.version}" ], { "lib.version": "1.0" }, "pom" );
		artifact( "lib", "1.0" );
		artifact( "child", "1.0", [ "lib:" ], [], {}, "jar", "parent:1.0" );
	}

	function afterAll() {
		if ( directoryExists( variables.repo ) ) directoryDelete( variables.repo, true );
	}

	function run( testResults, testBox ) {
		describe( "LDEV-6187: POM initialisation without the sleep workaround", function() {

			it( "resolves dependencies and dependencyManagement inherited from a parent pom", function() {
				var ids = resolve( "child", "1.0" );
				expect( ids ).toBe( "lib:1.0" );
			});

			it( "resolves the same pom from many threads at the same time", function() {
				var POM = createObject( "java", "lucee.runtime.mvn.POM" );
				var dir = repoResource();
				var pom = POM.getInstance( dir, variables.group, "child", "1.0", nullValue() );
				var names = [];
				loop from=1 to=10 index="local.i" {
					arrayAppend( names, "ldev6187-" & i );
				}
				loop array=names item="local.n" {
					thread action="run" name=n pom=pom {
						try {
							attributes.pom.getJarPOMs( false );
						}
						catch ( any e ) {
							thread.error = e.message;
						}
					}
				}
				thread action="join" name=arrayToList( names );
				loop array=names item="local.n" {
					var t = cfthread[ n ];
					expect( structKeyExists( t, "error" ) ? t.error : "" ).toBe( "" );
				}
				expect( arrayLen( pom.getJarPOMs( false ).toArray() ) ).toBe( 2 );
			});

			it( "a pom that cannot be loaded fails with a message naming the artifact, not with a NullPointerException", function() {
				var POM = createObject( "java", "lucee.runtime.mvn.POM" );
				var pom = POM.getInstance( repoResource(), variables.group, "doesnotexist", "9.9.9", nullValue() );
				var msg = "";
				try {
					pom.getJarPOMs( false );
				}
				catch ( any e ) {
					msg = e.message;
				}
				expect( msg ).notToBe( "", "loading a missing pom must fail" );
				expect( msg ).toInclude( "doesnotexist" );
				expect( msg ).notToInclude( "because" );
				expect( msg ).notToInclude( "is null" );
			});

		});
	}

	private any function repoResource() {
		return createObject( "java", "lucee.commons.io.res.ResourcesImpl" ).getFileResourceProvider().getResource( variables.repo );
	}

	// returns the direct and transitive jar poms of the artifact as "artifactId:version" list
	private string function resolve( required string artifactId, required string version ) {
		var POM = createObject( "java", "lucee.runtime.mvn.POM" );
		var root = POM.getInstance( repoResource(), variables.group, arguments.artifactId, arguments.version, nullValue() );
		var result = [];
		loop array=root.getJarPOMs( false ).toArray() item="local.p" {
			if ( p.getArtifactId() == arguments.artifactId ) continue;
			arrayAppend( result, p.getArtifactId() & ":" & p.getVersion() );
		}
		return arrayToList( result );
	}

	// writes a pom (and a jar unless the packaging is pom) into the local repository
	// dependencies "artifactId:version" (empty version = taken from dependencyManagement)
	private void function artifact( required string artifactId, required string version, array dependencies = [], array managed = [], struct properties = {}, string packaging = "jar", string parent = "" ) {
		var dir = variables.repo & replace( variables.group, ".", "/", "all" ) & "/" & arguments.artifactId & "/" & arguments.version & "/";
		directoryCreate( dir, true );
		var deps = "";
		loop array=arguments.dependencies item="local.dep" {
			deps &= "<dependency><groupId>#variables.group#</groupId><artifactId>#listFirst( dep, ':' )#</artifactId>" & ( len( listLast( dep, ':' ) ) && listLen( dep, ':' ) > 1 ? "<version>#listLast( dep, ':' )#</version>" : "" ) & "</dependency>";
		}
		var mgmt = "";
		loop array=arguments.managed item="local.m" {
			mgmt &= "<dependency><groupId>#variables.group#</groupId><artifactId>#listFirst( m, ':' )#</artifactId><version>#listLast( m, ':' )#</version></dependency>";
		}
		var props = "";
		loop struct=arguments.properties index="local.k" item="local.v" {
			props &= "<#k#>#v#</#k#>";
		}
		var par = "";
		if ( len( arguments.parent ) ) {
			par = "<parent><groupId>#variables.group#</groupId><artifactId>#listFirst( arguments.parent, ':' )#</artifactId><version>#listLast( arguments.parent, ':' )#</version></parent>";
		}
		fileWrite( dir & arguments.artifactId & "-" & arguments.version & ".pom", '<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0">
	<modelVersion>4.0.0</modelVersion>
	#par#
	<groupId>#variables.group#</groupId>
	<artifactId>#arguments.artifactId#</artifactId>
	<version>#arguments.version#</version>
	<packaging>#arguments.packaging#</packaging>
	<properties>#props#</properties>
	<dependencyManagement><dependencies>#mgmt#</dependencies></dependencyManagement>
	<dependencies>#deps#</dependencies>
</project>' );
		if ( arguments.packaging == "pom" ) return;
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
