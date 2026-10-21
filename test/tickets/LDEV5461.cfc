component extends="org.lucee.cfml.test.LuceeTestCase" labels="inspectTemplates,application" {

	function beforeAll() {
		variables.uri = createURI( "LDEV5461" );
		variables.subDir = getDirectoryFromPath( getCurrentTemplatePath() ) & "LDEV5461/sub/";
		removeChildApplication();
	}

	function afterAll() {
		removeChildApplication();
	}

	function run( testResults, testBox ) {
		describe( "LDEV-5461 inspectTemplates() and pagePoolClear() clear the Application.cfc path cache", function() {

			it( title="inspectTemplates() picks up a newly added Application.cfc right away", body=function() {
				expect( checkName() ).toBe( "ldev5461-parent" );
				addChildApplication( "ldev5461-child-inspect" );
				inspectTemplates();
				expect( checkName() ).toBe( "ldev5461-child-inspect" );
				removeChildApplication();
				inspectTemplates();
				expect( checkName() ).toBe( "ldev5461-parent" );
			});

			it( title="pagePoolClear() picks up a newly added Application.cfc right away", body=function() {
				expect( checkName() ).toBe( "ldev5461-parent" );
				addChildApplication( "ldev5461-child-pagepool" );
				pagePoolClear();
				expect( checkName() ).toBe( "ldev5461-child-pagepool" );
				removeChildApplication();
				pagePoolClear();
				expect( checkName() ).toBe( "ldev5461-parent" );
			});

			it( title="pagePoolClear( force=true ) picks up a newly added Application.cfc right away", body=function() {
				expect( checkName() ).toBe( "ldev5461-parent" );
				addChildApplication( "ldev5461-child-force" );
				pagePoolClear( true );
				expect( checkName() ).toBe( "ldev5461-child-force" );
				removeChildApplication();
				pagePoolClear( true );
				expect( checkName() ).toBe( "ldev5461-parent" );
			});

		});
	}

	private string function checkName() {
		var result = _InternalRequest( template: "#variables.uri#/sub/index.cfm" );
		return trim( result.filecontent );
	}

	private void function addChildApplication( required string name ) {
		fileWrite( variables.subDir & "Application.cfc", 'component { this.name = "#arguments.name#"; }' );
	}

	private void function removeChildApplication() {
		if ( fileExists( variables.subDir & "Application.cfc" ) ) fileDelete( variables.subDir & "Application.cfc" );
	}

	private string function createURI( string calledName ) {
		var baseURI = "/test/#listLast( getDirectoryFromPath( getCurrentTemplatePath() ), "\/" )#/";
		return baseURI & "" & calledName;
	}
}
