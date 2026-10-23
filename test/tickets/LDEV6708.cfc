component extends="org.lucee.cfml.test.LuceeTestCase" labels="maven" {

	function beforeAll() {
		variables.rootDir = getDirectoryFromPath( getCurrentTemplatePath() ) & "../../";
		variables.pom = xmlParse( fileRead( rootDir & "loader/pom.xml" ) );
		variables.workflowLines = listToArray( fileRead( rootDir & ".github/workflows/main.yml" ), chr( 10 ) );
	}

	private array function names( required array nodes ) {
		var result = [];
		arrayEach( nodes, function( n ) { arrayAppend( result, trim( n.xmlText ) ); } );
		return result;
	}

	private function centralPlugin( required string artifactId ) {
		var plugins = xmlSearch( pom, "//*[local-name()='profile'][*[local-name()='id']='maven-central']//*[local-name()='plugin'][*[local-name()='artifactId']='#artifactId#']" );
		expect( arrayLen( plugins ) ).toBe( 1, "plugin [#artifactId#] in profile maven-central" );
		return plugins[ 1 ];
	}

	function run( testResults, testBox ) {
		describe( "LDEV-6708 a Lucee 9 release only publishes the main jar on Maven Central", function() {

			it( "the maven-central profile attaches none of the other build artifacts", function() {
				var attached = names( xmlSearch( pom, "/*[local-name()='project']/*[local-name()='build']//*[local-name()='plugin'][*[local-name()='artifactId']='build-helper-maven-plugin']//*[local-name()='execution']/*[local-name()='id']" ) );
				expect( arrayLen( attached ) ).toBeGT( 0 );
				var plugin = centralPlugin( "build-helper-maven-plugin" );
				var disabled = names( xmlSearch( plugin, ".//*[local-name()='execution'][*[local-name()='phase']='none']/*[local-name()='id']" ) );
				for ( var id in attached ) {
					expect( disabled ).toInclude( id );
				}
			});

			it( "the maven-central profile replaces the full javadoc with a minimal javadoc jar", function() {
				var javadoc = centralPlugin( "maven-javadoc-plugin" );
				expect( names( xmlSearch( javadoc, ".//*[local-name()='execution'][*[local-name()='phase']='none']/*[local-name()='id']" ) ) ).toInclude( "attach-javadocs" );

				var jar = centralPlugin( "maven-jar-plugin" );
				var classifier = xmlSearch( jar, ".//*[local-name()='classifier']" );
				expect( arrayLen( classifier ) ).toBe( 1 );
				expect( classifier[ 1 ].xmlText ).toBe( "javadoc" );
				var dir = replace( xmlSearch( jar, ".//*[local-name()='classesDirectory']" )[ 1 ].xmlText, "${project.basedir}", rootDir & "loader" );
				expect( directoryExists( dir ) ).toBeTrue( dir );
				expect( arrayLen( directoryList( dir, true, "path", "*", "", "file" ) ) ).toBeGT( 0 );
			});

			it( "the maven-central profile keeps the sources jar", function() {
				var sources = xmlSearch( pom, "//*[local-name()='profile'][*[local-name()='id']='maven-central']//*[local-name()='plugin'][*[local-name()='artifactId']='maven-source-plugin']" );
				expect( arrayLen( sources ) ).toBe( 0 );
				expect( arrayLen( xmlSearch( pom, "/*[local-name()='project']/*[local-name()='build']//*[local-name()='plugin'][*[local-name()='artifactId']='maven-source-plugin']" ) ) ).toBe( 1 );
			});

			it( "the build workflow deploys releases with the maven-central profile and pre-releases not at all", function() {
				var deploys = [];
				arrayEach( workflowLines, function( line, i ) {
					if ( find( "mvn ", line ) && find( " deploy", line ) ) arrayAppend( deploys, { line: trim( line ), index: i } );
				});
				expect( arrayLen( deploys ) ).toBe( 2, serializeJSON( deploys ) );
				var snapshot = deploys.filter( function( d ) { return findNoCase( "SNAPSHOT", workflowLines[ d.index - 1 ] ); } );
				var release = deploys.filter( function( d ) { return find( "-DperformRelease=true", d.line ); } );
				expect( arrayLen( snapshot ) ).toBe( 1, serializeJSON( deploys ) );
				expect( arrayLen( release ) ).toBe( 1, serializeJSON( deploys ) );
				expect( release[ 1 ].line ).toInclude( "-Pmaven-central" );
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
