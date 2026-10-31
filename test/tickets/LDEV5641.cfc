component extends="org.lucee.cfml.test.LuceeTestCase" labels="maven" {

	function beforeAll() {
		variables.repo = getTempDirectory() & "LDEV5641-" & createUUID() & "/";
		variables.group = "org.lucee.test.ldev5641.g" & lCase( replace( createUUID(), "-", "", "all" ) );
		directoryCreate( repo, true );

		// the maven directory of the server, used by mavenLoad()
		variables.mavenDir = getPageContext().getConfig().getMavenDir().getAbsolutePath() & "/";

		// root:1.0 (parent: base:1.0, which declares inherited:1.0)
		//   a:1.0
		//     c:1.0
		//       e:1.0
		//   b:1.0
		//     d:1.0
		artifact( "base", "1.0", [ "inherited:1.0" ] );
		artifact( "inherited", "1.0" );
		artifact( "root", "1.0", [ "a:1.0", "b:1.0" ], "base:1.0" );
		artifact( "a", "1.0", [ "c:1.0" ] );
		artifact( "b", "1.0", [ "d:1.0" ] );
		artifact( "c", "1.0", [ "e:1.0" ] );
		artifact( "d", "1.0" );
		artifact( "e", "1.0" );
	}

	function afterAll() {
		if ( directoryExists( variables.repo ) ) directoryDelete( variables.repo, true );
		var groupDir = variables.mavenDir & replace( variables.group, ".", "/", "all" );
		if ( directoryExists( groupDir ) ) directoryDelete( groupDir, true );
	}

	function run( testResults, testBox ) {
		describe( "LDEV-5641: Maven transitive and exclusions", function() {

			describe( "POM resolution", function() {

				it( "resolves all dependencies by default", function() {
					expect( resolve() ).toBe( "a,b,c,d,e,inherited,root" );
				});

				it( "transitive=false resolves only the artifact itself", function() {
					expect( resolve( false ) ).toBe( "root" );
				});

				it( "an exclusion removes a direct dependency", function() {
					expect( resolve( true, [ "#variables.group#:a" ] ) ).toBe( "b,d,inherited,root" );
				});

				it( "an exclusion removes a deeper dependency", function() {
					expect( resolve( true, [ "#variables.group#:d" ] ) ).toBe( "a,b,c,e,inherited,root" );
				});

				it( "the dependencies of an excluded artifact are not resolved", function() {
					expect( resolve( true, [ "#variables.group#:c" ] ) ).toBe( "a,b,d,inherited,root" );
				});

				it( "an exclusion removes a dependency inherited from the parent POM", function() {
					expect( resolve( true, [ "#variables.group#:inherited" ] ) ).toBe( "a,b,c,d,e,root" );
				});

				it( "wildcard in the artifactId", function() {
					expect( resolve( true, [ "#variables.group#:inh*" ] ) ).toBe( "a,b,c,d,e,root" );
				});

				it( "wildcard for the groupId", function() {
					expect( resolve( true, [ "*:c" ] ) ).toBe( "a,b,d,inherited,root" );
				});

				it( "wildcard for the whole artifact list of a group", function() {
					expect( resolve( true, [ "#variables.group#:*" ] ) ).toBe( "root" );
				});

				it( "multiple exclusions", function() {
					expect( resolve( true, [ "#variables.group#:a", "#variables.group#:inherited" ] ) ).toBe( "b,d,root" );
				});

				it( "an exclusion that matches nothing changes nothing", function() {
					expect( resolve( true, [ "other.group:*", "#variables.group#:unknown" ] ) ).toBe( "a,b,c,d,e,inherited,root" );
				});

				it( "options of one resolution do not leak into another one", function() {
					resolve( false );
					resolve( true, [ "#variables.group#:*" ] );
					expect( resolve() ).toBe( "a,b,c,d,e,inherited,root" );
				});

			});

			describe( "mavenLoad()", function() {

				it( "loads all dependencies by default", function() {
					expect( load( { "groupId": variables.group, "artifactId": "root", "version": "1.0" } ) ).toBe( "a,b,c,d,e,inherited,root" );
				});

				it( "transitive=false loads only the artifact itself", function() {
					expect( load( { "groupId": variables.group, "artifactId": "root", "version": "1.0", "transitive": false } ) ).toBe( "root" );
				});

				it( "transitive=true is the default", function() {
					expect( load( { "groupId": variables.group, "artifactId": "root", "version": "1.0", "transitive": true } ) ).toBe( "a,b,c,d,e,inherited,root" );
				});

				it( "exclusions as array", function() {
					expect( load( { "groupId": variables.group, "artifactId": "root", "version": "1.0", "exclusions": [ "#variables.group#:a", "#variables.group#:d" ] } ) ).toBe( "b,inherited,root" );
				});

				it( "exclusions as comma separated list", function() {
					expect( load( { "groupId": variables.group, "artifactId": "root", "version": "1.0", "exclusions": "#variables.group#:a, #variables.group#:d" } ) ).toBe( "b,inherited,root" );
				});

				it( "exclusion with wildcard", function() {
					expect( load( { "groupId": variables.group, "artifactId": "root", "version": "1.0", "exclusions": [ "#variables.group#:*" ] } ) ).toBe( "root" );
				});

				it( "invalid exclusion throws", function() {
					expect( function() {
						mavenLoad( { "groupId": variables.group, "artifactId": "root", "version": "1.0", "exclusions": [ "no-artifact-part" ] } );
					}).toThrow( message="Invalid Maven exclusion" );
				});

				it( "invalid transitive throws", function() {
					expect( function() {
						mavenLoad( { "groupId": variables.group, "artifactId": "root", "version": "1.0", "transitive": "maybe" } );
					}).toThrow( message="transitive" );
				});

			});

			describe( "javaSettings", function() {

				// guava depends on com.google.guava:failureaccess (not bundled with Lucee)
				it( "loads the dependencies by default", function() {
					var cmp = new component javaSettings='{"maven":[{"groupId":"com.google.guava","artifactId":"guava","version":"33.0.0-jre"}]}' {
						function load( className ) {
							createObject( "java", className );
							return className;
						}
					};
					expect( cmp.load( "com.google.common.util.concurrent.internal.InternalFutureFailureAccess" ) ).toBe( "com.google.common.util.concurrent.internal.InternalFutureFailureAccess" );
				});

				it( "transitive=false only loads the artifact itself", function() {
					var cmp = new component javaSettings='{"maven":[{"groupId":"com.google.guava","artifactId":"guava","version":"33.0.0-jre","transitive":false}]}' {
						function load( className ) {
							createObject( "java", className );
							return className;
						}
					};
					expect( cmp.load( "com.google.common.collect.ImmutableList" ) ).toBe( "com.google.common.collect.ImmutableList" );
					expect( function() {
						cmp.load( "com.google.common.util.concurrent.internal.InternalFutureFailureAccess" );
					}).toThrow();
				});

				it( "exclusions skip the matching dependencies", function() {
					var cmp = new component javaSettings='{"maven":[{"groupId":"com.google.guava","artifactId":"guava","version":"33.0.0-jre","exclusions":["com.google.guava:failureaccess"]}]}' {
						function load( className ) {
							createObject( "java", className );
							return className;
						}
					};
					expect( cmp.load( "com.google.common.collect.ImmutableList" ) ).toBe( "com.google.common.collect.ImmutableList" );
					expect( function() {
						cmp.load( "com.google.common.util.concurrent.internal.InternalFutureFailureAccess" );
					}).toThrow();
				});

			});

		});
	}

	// sorted artifactIds returned by POM.getJarPOMs()
	private string function resolve( boolean transitive = true, array exclusions = [] ) {
		var POM = createObject( "java", "lucee.runtime.mvn.POM" );
		var dir = createObject( "java", "lucee.commons.io.res.ResourcesImpl" ).getFileResourceProvider().getResource( variables.repo );
		var root = POM.getInstance( dir, variables.group, "root", "1.0", nullValue(), nullValue(), POM.SCOPES_FOR_RUNTIME, arguments.transitive, arguments.exclusions, nullValue() );
		var ids = [];
		loop array=root.getJarPOMs( false ).toArray() item="local.p" {
			expect( p.getGroupId() ).toBe( variables.group );
			arrayAppend( ids, p.getArtifactId() );
		}
		return listSort( arrayToList( ids ), "text" );
	}

	// sorted artifactIds of the jars returned by mavenLoad()
	private string function load( required struct coordinate ) {
		var ids = [];
		loop array=mavenLoad( arguments.coordinate ) item="local.jar" {
			arrayAppend( ids, listFirst( listLast( jar, "/\" ), "-" ) );
		}
		return listSort( arrayToList( ids ), "text" );
	}

	// writes a pom and a jar into the local repository and the maven directory
	private void function artifact( required string artifactId, required string version, array dependencies = [], string parent = "" ) {
		var path = replace( variables.group, ".", "/", "all" ) & "/" & arguments.artifactId & "/" & arguments.version & "/";
		var deps = "";
		loop array=arguments.dependencies item="local.dep" {
			deps &= "<dependency><groupId>#variables.group#</groupId><artifactId>#listFirst( dep, ':' )#</artifactId><version>#listLast( dep, ':' )#</version></dependency>";
		}
		var parentXml = "";
		if ( len( arguments.parent ) ) {
			parentXml = "<parent><groupId>#variables.group#</groupId><artifactId>#listFirst( arguments.parent, ':' )#</artifactId><version>#listLast( arguments.parent, ':' )#</version></parent>";
		}
		var pom = '<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0">
	<modelVersion>4.0.0</modelVersion>
	#parentXml#
	<groupId>#variables.group#</groupId>
	<artifactId>#arguments.artifactId#</artifactId>
	<version>#arguments.version#</version>
	<packaging>#arguments.artifactId == "base" ? "pom" : "jar"#</packaging>
	<dependencies>#deps#</dependencies>
</project>';
		loop array=[ variables.repo, variables.mavenDir ] item="local.base" {
			var dir = base & path;
			directoryCreate( dir, true, true );
			fileWrite( dir & arguments.artifactId & "-" & arguments.version & ".pom", pom );
			if ( arguments.artifactId == "base" ) continue;
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
}
