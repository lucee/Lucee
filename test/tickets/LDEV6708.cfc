component extends="org.lucee.cfml.test.LuceeTestCase" labels="maven" {

	function beforeAll() {
		variables.rootDir = getDirectoryFromPath( getCurrentTemplatePath() ) & "../../";
		variables.pom = xmlParse( fileRead( rootDir & "pom.xml" ) );
		variables.workflowLines = listToArray( fileRead( rootDir & ".github/workflows/main.yml" ), chr( 10 ) );
	}

	private array function names( required array nodes ) {
		var result = [];
		arrayEach( nodes, function( n ) { arrayAppend( result, trim( n.xmlText ) ); } );
		return result;
	}

	function run( testResults, testBox ) {
		describe( "LDEV-6708 a Lucee 9 release only publishes the main jar on Maven Central", function() {

			it( "the root pom attaches none of the other build artifacts (light, zero, json are built by the workflow)", function() {
				var attached = xmlSearch( pom, "/*[local-name()='project']/*[local-name()='build']//*[local-name()='plugin'][*[local-name()='artifactId']='build-helper-maven-plugin']//*[local-name()='goal'][text()='attach-artifact']" );
				expect( arrayLen( attached ) ).toBe( 0 );
			});

			it( "the root pom attaches a minimal javadoc jar instead of the full javadoc", function() {
				expect( arrayLen( xmlSearch( pom, "//*[local-name()='plugin'][*[local-name()='artifactId']='maven-javadoc-plugin']" ) ) ).toBe( 0 );
				var jar = xmlSearch( pom, "//*[local-name()='plugin'][*[local-name()='artifactId']='maven-jar-plugin']//*[local-name()='execution'][*[local-name()='id']='attach-minimal-javadoc']" );
				expect( arrayLen( jar ) ).toBe( 1 );
				var classifier = xmlSearch( jar[ 1 ], ".//*[local-name()='classifier']" );
				expect( arrayLen( classifier ) ).toBe( 1 );
				expect( classifier[ 1 ].xmlText ).toBe( "javadoc" );
				var dir = replace( xmlSearch( jar[ 1 ], ".//*[local-name()='classesDirectory']" )[ 1 ].xmlText, "${project.basedir}", rootDir );
				expect( directoryExists( dir ) ).toBeTrue( dir );
				expect( arrayLen( directoryList( dir, true, "path", "*", "", "file" ) ) ).toBeGT( 0 );
			});

			it( "the root pom keeps the sources jar", function() {
				expect( arrayLen( xmlSearch( pom, "/*[local-name()='project']/*[local-name()='build']//*[local-name()='plugin'][*[local-name()='artifactId']='maven-source-plugin']" ) ) ).toBe( 1 );
			});

			it( "the build workflow deploys releases to Maven Central and pre-releases not at all", function() {
				var deploys = [];
				arrayEach( workflowLines, function( line, i ) {
					if ( find( "mvn ", line ) && find( " deploy", line ) ) arrayAppend( deploys, { line: trim( line ), index: i } );
				});
				expect( arrayLen( deploys ) ).toBe( 2, serializeJSON( deploys ) );
				var snapshot = deploys.filter( function( d ) { return findNoCase( "SNAPSHOT", workflowLines[ d.index - 1 ] ); } );
				var release = deploys.filter( function( d ) { return find( "-DperformRelease=true", d.line ); } );
				expect( arrayLen( snapshot ) ).toBe( 1, serializeJSON( deploys ) );
				expect( arrayLen( release ) ).toBe( 1, serializeJSON( deploys ) );
				// versions with a qualifier (RC, BETA, ...) are installed before the release deploy is reached
				var preRelease = 0;
				arrayEach( workflowLines, function( line, i ) {
					if ( !preRelease && find( '"$VERSION_CLEAN" == *-*', line ) ) preRelease = i;
				});
				expect( preRelease ).toBeGT( 0 );
				expect( preRelease ).toBeLT( release[ 1 ].index );
				for ( var i = preRelease; i < release[ 1 ].index; i++ ) {
					if ( find( "mvn ", workflowLines[ i ] ) ) expect( workflowLines[ i ] ).toInclude( " install" );
				}
			});

		});
	}
}
