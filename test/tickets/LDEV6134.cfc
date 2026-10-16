component extends="org.lucee.cfml.test.LuceeTestCase" labels="bytecode" {

	function beforeAll() {
		generateFixture();
	}

	function run( testResults, testBox ) {
		describe( "LDEV-6134 - MethodTooLargeException in <cinit> when CFC has thousands of unique string keys", function() {
			it( title="component with 3000 keys compiles and instantiates without MethodTooLargeException", body=function( currentSpec ) {
				var obj = new LDEV6134.CfcWithExcessStaticObjectKeys( parent={} );
				expect( obj ).notToBeNull();
				expect( obj.testWord( "word_000001" ) ).toBe( "word_000001" );
			});
		});
	}

	private string function getFixtureDir() {
		return getDirectoryFromPath( getCurrentTemplatePath() ) & "LDEV6134/";
	}

	private string function getFixturePath() {
		return getFixtureDir() & "CfcWithExcessStaticObjectKeys.cfc";
	}

	private void function generateFixture() {
		var dir = getFixtureDir();
		if ( !directoryExists( dir ) ) directoryCreate( dir );
		var path = getFixturePath();
		// cleanup any stale fixture from a previous run
		if ( fileExists( path ) ) fileDelete( path );

		var keyCount = 3000;
		var nl = chr( 10 );
		var cfml = 'component {#nl##nl#';
		cfml &= '	function init( parent ) {#nl#';
		cfml &= '		return this;#nl#';
		cfml &= '	}#nl##nl#';
		cfml &= '	string function testWord( required string word ) {#nl#';
		cfml &= '		var words = {};#nl#';
		loop from="1" to="#keyCount#" index="local.i" {
			var key = "word_#numberFormat( i, '000000' )#";
			cfml &= '		words["#key#"] = "#key#";#nl#';
		}
		cfml &= '		return words[ARGUMENTS.word];#nl#';
		cfml &= '	}#nl#';
		cfml &= '}#nl#';
		fileWrite( path, cfml );
	}

}
