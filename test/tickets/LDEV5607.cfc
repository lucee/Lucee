component extends="org.lucee.cfml.test.LuceeTestCase" labels="deploy,maven,javasettings" {

	function beforeAll() {
		variables.cs = createObject( "java", "lucee.runtime.config.ConfigUtil" ).getConfigServerImpl( getPageContext().getConfig() );
		variables.deployDir = cs.getDeployDirectory().getAbsolutePath() & "/";
		variables.failedDir = deployDir & "failed-to-deploy/";
		variables.deployedDir = cs.getConfigDir().getAbsolutePath() & "/deployed-poms/";
		variables.artifacts = [ "javax.inject", "ldev5607-property", "ldev5607-managed" ];
		variables.names = [];
		cleanupConfig();
	}

	function afterAll() {
		cleanupConfig();
		loop array=variables.names item="local.name" {
			if ( fileExists( deployDir & name ) ) fileDelete( deployDir & name );
			if ( fileExists( failedDir & name ) ) fileDelete( failedDir & name );
			if ( fileExists( deployedDir & name ) ) fileDelete( deployedDir & name );
		}
	}

	function run( testResults, testBox ) {
		describe( "LDEV-5607: a pom in the deploy folder installs its dependencies", function() {

			afterEach( function() {
				cleanupConfig();
			});

			it( "installs the dependencies of a pom.xml server wide", function() {
				var name = "pom.xml";
				drop( name, pom( dependency( "javax.inject", "javax.inject", "1" ) ) );
				deploy();

				expect( fileExists( deployDir & name ) ).toBeFalse( "the pom is still in the deploy folder" );
				expect( fileExists( failedDir & name ) ).toBeFalse( "the pom was moved to the failed-to-deploy folder" );
				expect( fileExists( deployedDir & name ) ).toBeTrue( "the pom was not moved to the folder of the deployed poms" );
				expect( hasEntry( "javax.inject", "javax.inject", "1" ) ).toBeTrue( "the dependency is not part of the server javasettings" );
				expect( serverJars() ).toInclude( "javax.inject-1.jar" );
			});

			it( "installs the dependencies of a *.pom file", function() {
				var name = "ldev5607-" & createUUID() & ".pom";
				drop( name, pom( dependency( "javax.inject", "javax.inject", "1" ) ) );
				deploy();

				expect( fileExists( deployDir & name ) ).toBeFalse();
				expect( fileExists( failedDir & name ) ).toBeFalse();
				expect( hasEntry( "javax.inject", "javax.inject", "1" ) ).toBeTrue();
			});

			it( "resolves versions from properties and dependencyManagement", function() {
				var name = "ldev5607-" & createUUID() & ".pom";
				var deps = "<dependency><groupId>javax.inject</groupId><artifactId>javax.inject</artifactId><version>${inject.version}</version></dependency>";
				deps &= "<dependency><groupId>javax.inject</groupId><artifactId>javax.inject</artifactId><classifier>sources</classifier></dependency>";
				drop( name, pom( deps, "<properties><inject.version>1</inject.version></properties>"
					& "<dependencyManagement><dependencies><dependency><groupId>javax.inject</groupId><artifactId>javax.inject</artifactId><classifier>sources</classifier><version>1</version></dependency></dependencies></dependencyManagement>" ) );
				deploy();

				expect( fileExists( failedDir & name ) ).toBeFalse( "the pom was moved to the failed-to-deploy folder" );
				expect( hasEntry( "javax.inject", "javax.inject", "1" ) ).toBeTrue();
			});

			it( "ignores test, provided and optional dependencies and dependencies that are not a jar", function() {
				var name = "ldev5607-" & createUUID() & ".pom";
				var deps = dependency( "javax.inject", "javax.inject", "1" );
				deps &= dependency( "org.lucee.ldev5607", "does-not-exist-test", "1.0.0", "<scope>test</scope>" );
				deps &= dependency( "org.lucee.ldev5607", "does-not-exist-provided", "1.0.0", "<scope>provided</scope>" );
				deps &= dependency( "org.lucee.ldev5607", "does-not-exist-optional", "1.0.0", "<optional>true</optional>" );
				deps &= dependency( "org.lucee.ldev5607", "does-not-exist-pom", "1.0.0", "<type>pom</type>" );
				drop( name, pom( deps ) );
				deploy();

				expect( fileExists( failedDir & name ) ).toBeFalse( "the pom was moved to the failed-to-deploy folder" );
				expect( hasEntry( "javax.inject", "javax.inject", "1" ) ).toBeTrue();
				expect( hasEntry( "org.lucee.ldev5607", "does-not-exist-test" ) ).toBeFalse();
				expect( hasEntry( "org.lucee.ldev5607", "does-not-exist-provided" ) ).toBeFalse();
				expect( hasEntry( "org.lucee.ldev5607", "does-not-exist-optional" ) ).toBeFalse();
				expect( hasEntry( "org.lucee.ldev5607", "does-not-exist-pom" ) ).toBeFalse();
			});

			it( "moves the pom to the failed-to-deploy folder if a dependency can not be downloaded", function() {
				var name = "ldev5607-" & createUUID() & ".pom";
				drop( name, pom( dependency( "javax.inject", "javax.inject", "1" ) & dependency( "org.lucee.ldev5607", "does-not-exist", "1.0.0" ) ) );
				deploy();

				expect( fileExists( deployDir & name ) ).toBeFalse( "the pom is still in the deploy folder" );
				expect( fileExists( failedDir & name ) ).toBeTrue( "the pom was not moved to the failed-to-deploy folder" );
				expect( hasEntry( "org.lucee.ldev5607", "does-not-exist" ) ).toBeFalse( "the dependency was added to the configuration" );
				expect( hasEntry( "javax.inject", "javax.inject" ) ).toBeFalse( "a dependency was added to the configuration, but the pom failed" );
			});

			it( "fails a dependency without a version", function() {
				var name = "ldev5607-" & createUUID() & ".pom";
				drop( name, pom( "<dependency><groupId>javax.inject</groupId><artifactId>javax.inject</artifactId></dependency>" ) );
				deploy();

				expect( fileExists( failedDir & name ) ).toBeTrue( "the pom was not moved to the failed-to-deploy folder" );
				expect( hasEntry( "javax.inject", "javax.inject" ) ).toBeFalse();
			});

			it( "fails a dependency with an unresolved placeholder", function() {
				var name = "ldev5607-" & createUUID() & ".pom";
				drop( name, pom( dependency( "javax.inject", "javax.inject", "${does.not.exist}" ) ) );
				deploy();

				expect( fileExists( failedDir & name ) ).toBeTrue( "the pom was not moved to the failed-to-deploy folder" );
				expect( hasEntry( "javax.inject", "javax.inject" ) ).toBeFalse();
			});

			it( "fails a pom that is not valid XML", function() {
				var name = "ldev5607-" & createUUID() & ".pom";
				drop( name, "this is not a pom" );
				deploy();

				expect( fileExists( deployDir & name ) ).toBeFalse( "the pom is still in the deploy folder" );
				expect( fileExists( failedDir & name ) ).toBeTrue( "the pom was not moved to the failed-to-deploy folder" );
			});

			it( "accepts a pom without dependencies and does not change the configuration", function() {
				var name = "ldev5607-" & createUUID() & ".pom";
				drop( name, pom( "" ) );
				deploy();

				expect( fileExists( deployDir & name ) ).toBeFalse();
				expect( fileExists( failedDir & name ) ).toBeFalse();
				expect( fileExists( deployedDir & name ) ).toBeTrue();
			});

		});
	}

	private string function dependency( required string groupId, required string artifactId, required string version, string extra = "" ) {
		return "<dependency><groupId>#groupId#</groupId><artifactId>#artifactId#</artifactId><version>#version#</version>#extra#</dependency>";
	}

	private string function pom( required string dependencies, string extra = "" ) {
		return '<?xml version="1.0" encoding="UTF-8"?><project xmlns="http://maven.apache.org/POM/4.0.0"><modelVersion>4.0.0</modelVersion><groupId>org.lucee.ldev5607</groupId><artifactId>app</artifactId><version>1.0.0</version>#extra#<dependencies>#dependencies#</dependencies></project>';
	}

	private void function drop( required string name, required string content ) {
		arrayAppend( variables.names, name );
		fileWrite( deployDir & name, content );
	}

	private void function deploy() {
		createObject( "java", "lucee.runtime.config.DeployHandler" ).deploy( cs, cs.getLog( "deploy" ), true );
	}

	private array function entries() {
		var raw = cs.raw();
		if ( !structKeyExists( raw, "javasettings" ) ) return [];
		var maven = raw.javasettings.maven ?: raw.javasettings.mvn ?: [];
		return isArray( maven ) ? maven : [ maven ];
	}

	private boolean function hasEntry( required string groupId, required string artifactId, string version ) {
		loop array=entries() item="local.e" {
			if ( e.groupId == groupId && e.artifactId == artifactId && ( isNull( arguments.version ) || e.version == arguments.version ) ) return true;
		}
		return false;
	}

	private string function serverJars() {
		var js = cs.getJavaSettings();
		var jars = [];
		loop collection=js.getAllResources() item="local.res" {
			arrayAppend( jars, res.getName() );
		}
		return arrayToList( jars );
	}

	// removes the dependencies added by the tests from the server configuration
	private void function cleanupConfig() {
		var keep = [];
		var changed = false;
		loop array=entries() item="local.e" {
			if ( e.groupId == "javax.inject" || e.groupId == "org.lucee.ldev5607" ) changed = true;
			else arrayAppend( keep, e );
		}
		if ( changed ) {
			var raw = cs.raw();
			if ( structKeyExists( raw.javasettings, "mvn" ) ) raw.javasettings.mvn = keep;
			else raw.javasettings.maven = keep;
			createObject( "java", "lucee.runtime.config.ConfigAdmin" ).newInstance( cs, javaCast( "null", "" ), true ).store();
			cs.resetJavaSettings();
		}
	}

}
