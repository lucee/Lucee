component extends="org.lucee.cfml.test.LuceeTestCase" labels="maven" {

	function beforeAll() {
		variables.rootDir = getDirectoryFromPath( getCurrentTemplatePath() ) & "../../";
		variables.centralPom = xmlParse( fileRead( rootDir & "loader/central/pom.xml" ) );
		variables.workflowLines = listToArray( fileRead( rootDir & ".github/workflows/main.yml" ), chr( 10 ) );
	}

	function run( testResults, testBox ) {
		describe( "LDEV-6707 / LDEV-6900 a release only publishes the pom, the .lco and the main jar on Maven Central", function() {

			it( "the Maven Central pom is org.lucee:lucee with pom packaging", function() {
				var project = centralPom.xmlRoot;
				expect( project.groupId.xmlText ).toBe( "org.lucee" );
				expect( project.artifactId.xmlText ).toBe( "lucee" );
				expect( project.packaging.xmlText ).toBe( "pom" );
			});

			it( "the Maven Central pom has the metadata Central requires", function() {
				var project = centralPom.xmlRoot;
				expect( len( trim( project.name.xmlText ) ) ).toBeGT( 0 );
				expect( len( trim( project.description.xmlText ) ) ).toBeGT( 0 );
				expect( len( trim( project.url.xmlText ) ) ).toBeGT( 0 );
				expect( arrayLen( xmlSearch( centralPom, "//*[local-name()='license']" ) ) ).toBeGT( 0 );
				expect( arrayLen( xmlSearch( centralPom, "//*[local-name()='developer']" ) ) ).toBeGT( 0 );
				expect( arrayLen( xmlSearch( centralPom, "//*[local-name()='scm']" ) ) ).toBe( 1 );
			});

			it( "the artifacts attached to the Maven Central pom are the .lco, the main jar and the sources jar (LDEV-6900)", function() {
				var artifacts = xmlSearch( centralPom, "//*[local-name()='artifacts']/*[local-name()='artifact']" );
				expect( arrayLen( artifacts ) ).toBe( 3 );
				expect( artifacts[ 1 ].type.xmlText ).toBe( "lco" );
				expect( artifacts[ 1 ].file.xmlText ).toInclude( "lucee.core.file" );
				expect( xmlSearch( centralPom, "//*[local-name()='lucee.core.file']" )[ 1 ].xmlText ).toInclude( ".lco" );
				// the main jar, no classifier
				expect( artifacts[ 2 ].type.xmlText ).toBe( "jar" );
				expect( arrayLen( xmlSearch( artifacts[ 2 ], "./*[local-name()='classifier']" ) ) ).toBe( 0 );
				expect( artifacts[ 2 ].file.xmlText ).toInclude( "lucee.jar.file" );
				expect( xmlSearch( centralPom, "//*[local-name()='lucee.jar.file']" )[ 1 ].xmlText ).toInclude( "lucee-${project.version}.jar" );
				// the loader sources jar
				expect( artifacts[ 3 ].classifier.xmlText ).toBe( "sources" );
				// no light or zero on Maven Central
				var classifiers = [];
				arrayEach( xmlSearch( centralPom, "//*[local-name()='classifier']" ), function( c ) { arrayAppend( classifiers, trim( c.xmlText ) ); } );
				expect( arrayFind( classifiers, "light" ) ).toBe( 0 );
				expect( arrayFind( classifiers, "zero" ) ).toBe( 0 );
			});

			it( "the Maven Central pom builds a minimal javadoc jar (LDEV-6900)", function() {
				var jar = xmlSearch( centralPom, "//*[local-name()='plugin'][*[local-name()='artifactId']='maven-jar-plugin']" );
				expect( arrayLen( jar ) ).toBe( 1 );
				expect( xmlSearch( jar[ 1 ], ".//*[local-name()='classifier']" )[ 1 ].xmlText ).toBe( "javadoc" );
				var dir = replace( xmlSearch( jar[ 1 ], ".//*[local-name()='classesDirectory']" )[ 1 ].xmlText, "${project.basedir}", rootDir & "loader/central" );
				expect( directoryExists( dir ) ).toBeTrue( dir );
				expect( arrayLen( directoryList( dir, true, "path", "*", "", "file" ) ) ).toBeGT( 0 );
			});

			it( "the Maven Central pom uses neither the source nor the javadoc plugin (the sources jar comes from ../pom.xml, the javadoc jar is minimal)", function() {
				var plugins = xmlSearch( centralPom, "//*[local-name()='plugin']/*[local-name()='artifactId']" );
				var names = [];
				arrayEach( plugins, function( p ) { arrayAppend( names, p.xmlText ); } );
				expect( names ).toInclude( "central-publishing-maven-plugin" );
				expect( arrayFind( names, "maven-source-plugin" ) ).toBe( 0 );
				expect( arrayFind( names, "maven-javadoc-plugin" ) ).toBe( 0 );
			});

			it( "the build workflow only deploys the full loader build for snapshots", function() {
				var deploys = [];
				arrayEach( workflowLines, function( line, i ) {
					if ( find( "mvn ", line ) && find( " deploy", line ) ) arrayAppend( deploys, { line: trim( line ), index: i } );
				});
				var loaderDeploys = deploys.filter( function( d ) { return find( "-f loader/pom.xml", d.line ); } );
				var centralDeploys = deploys.filter( function( d ) { return find( "-f loader/central/pom.xml", d.line ); } );
				expect( arrayLen( loaderDeploys ) ).toBe( 1, "deploys of loader/pom.xml: " & serializeJSON( loaderDeploys ) );
				expect( workflowLines[ loaderDeploys[ 1 ].index - 1 ] ).toInclude( "SNAPSHOT" );
				expect( arrayLen( centralDeploys ) ).toBe( 1, "deploys of loader/central/pom.xml: " & serializeJSON( centralDeploys ) );
				expect( centralDeploys[ 1 ].line ).toInclude( "-DperformRelease=true" );
			});

		});
	}
}
