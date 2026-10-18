component extends="org.lucee.cfml.test.LuceeTestCase" labels="maven" {

	function beforeAll() {
		variables.groupId = "org.lucee.test.ldev6612";
		variables.artifactId = "local-only-#createUniqueId()#";
		variables.dir = getTempDirectory() & "ldev6612-" & createUUID() & "/";
		// these coordinates do not exist on any remote repository, so a remote lookup can only fail
		var artifactDir = dir & replace( groupId, ".", "/", "all" ) & "/" & artifactId & "/";
		for ( var v in [ "1.0.0", "1.2.0" ] ) {
			directoryCreate( artifactDir & v, true, true );
			fileWrite( artifactDir & v & "/" & artifactId & "-" & v & ".pom", '<?xml version="1.0" encoding="UTF-8"?>'
				& '<project xmlns="http://maven.apache.org/POM/4.0.0"><modelVersion>4.0.0</modelVersion>'
				& '<groupId>#groupId#</groupId><artifactId>#artifactId#</artifactId><version>#v#</version>'
				& '<packaging>pom</packaging></project>' );
		}
		// a higher version folder without jar or pom (e.g. an aborted download) must be ignored
		directoryCreate( artifactDir & "2.0.0", true, true );
	}

	function afterAll() {
		if ( directoryExists( variables.dir ) ) directoryDelete( variables.dir, true );
	}

	function run( testResults, testBox ) {
		describe( "LDEV-6612 maven coordinate without version", function() {
			it( "uses the highest locally installed version, no remote lookup", function() {
				var res = createObject( "java", "lucee.commons.io.res.util.ResourceUtil" ).toResourceExisting( getPageContext(), variables.dir );
				var pom = createObject( "java", "lucee.runtime.mvn.POM" ).getInstance( res, variables.groupId, variables.artifactId, javacast( "null", "" ), javacast( "null", "" ) );
				expect( pom.getVersion() ).toBe( "1.2.0" );
			});
		});
	}
}
