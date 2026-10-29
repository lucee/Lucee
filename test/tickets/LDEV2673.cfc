component extends="org.lucee.cfml.test.LuceeTestCase" {

	function beforeAll(){
		variables.uri = createURI( "LDEV2673" );
		variables.pool = getPageContext().getConfig().getDebuggerPool();
		setDebug( true );
		setMaxLogs( 3 );
	}

	function afterAll(){
		setDebug( false );
		setMaxLogs( "" );
		purge();
	}

	function run( testResults, testBox ){
		describe( "LDEV-2673 debug logs are held by soft references", function(){

			beforeEach( function(){
				purge();
			} );

			it( "stores debug data and returns it", function(){
				var id = doRequest();
				var logs = getLogs();
				expect( logs ).toHaveLength( 1 );
				expect( logs[ 1 ].id ).toBe( id );
				expect( pool.size() ).toBe( 1 );
			} );

			it( "returns a single entry by id", function(){
				var id1 = doRequest();
				var id2 = doRequest();
				expect( getLogs() ).toHaveLength( 2 );
				admin action="getLoggedDebugData" type="server" password="#request.SERVERADMINPASSWORD#" id="#id1#" returnVariable="local.entry";
				expect( entry.id ).toBe( id1 );
				expect( entry.id ).notToBe( id2 );
			} );

			it( "keeps only the newest entries up to the max count", function(){
				var ids = [];
				loop times=5 {
					arrayAppend( ids, doRequest() );
				}
				var logs = getLogs();
				expect( logs ).toHaveLength( 3 );
				expect( logs[ 1 ].id ).toBe( ids[ 3 ] );
				expect( logs[ 2 ].id ).toBe( ids[ 4 ] );
				expect( logs[ 3 ].id ).toBe( ids[ 5 ] );
				expect( pool.size() ).toBe( 3 );
			} );

			it( "returns no entries when all references have been cleared", function(){
				doRequest();
				doRequest();
				expect( getLogs() ).toHaveLength( 2 );

				pool.clearReferences();

				expect( getLogs() ).toHaveLength( 0 );
				expect( pool.size() ).toBe( 0 );
			} );

			it( "reports an id of a cleared entry as not found", function(){
				var id = doRequest();
				pool.clearReferences();
				var caught = nullValue();
				try {
					admin action="getLoggedDebugData" type="server" password="#request.SERVERADMINPASSWORD#" id="#id#" returnVariable="local.entry";
				}
				catch ( any e ) {
					caught = e;
				}
				expect( isNull( caught ) ).toBeFalse( "expected an exception for the cleared entry" );
				expect( caught.type ).toBe( "application" );
				expect( caught.message ).toBe( "No debugging data with id [#id#] found." );
			} );

			it( "removes cleared entries on store and they do not count against the max", function(){
				loop times=3 {
					doRequest();
				}
				expect( pool.size() ).toBe( 3 );
				pool.clearReferences();
				expect( pool.size() ).toBe( 3 );

				var ids = [];
				loop times=2 {
					arrayAppend( ids, doRequest() );
				}
				var logs = getLogs();
				expect( pool.size() ).toBe( 2 );
				expect( logs ).toHaveLength( 2 );
				expect( logs[ 1 ].id ).toBe( ids[ 1 ] );
				expect( logs[ 2 ].id ).toBe( ids[ 2 ] );
			} );

		} );
	}

	private string function doRequest(){
		_internalRequest( template: "#uri#/test.cfm" );
		var logs = getLogs();
		return logs[ arrayLen( logs ) ].id;
	}

	private array function getLogs(){
		var logs = [];
		admin action="getLoggedDebugData" type="server" password="#request.SERVERADMINPASSWORD#" returnVariable="logs";
		return logs;
	}

	private void function purge(){
		admin action="purgeDebugPool" type="server" password="#request.SERVERADMINPASSWORD#";
	}

	private void function setDebug( required boolean enabled ){
		admin action="updateDebug" type="server" password="#request.SERVERADMINPASSWORD#" debug="#arguments.enabled#" template="#arguments.enabled#";
	}

	private void function setMaxLogs( required any maxLogs ){
		admin action="updateDebugSetting" type="server" password="#request.SERVERADMINPASSWORD#" maxLogs="#arguments.maxLogs#";
	}

	private string function createURI( string calledName ){
		var baseURI = "/test/#listLast( getDirectoryFromPath( getCurrentTemplatePath() ), "\/" )#/";
		return baseURI & "" & calledName;
	}

}
