component extends="org.lucee.cfml.test.LuceeTestCase" labels="debugging,query" {

	function beforeAll() {
		variables.uri = createURI( "LDEV3381" );
		enableDebug();
	}

	function afterAll() {
		disableDebug();
	}

	function run( testResults, testBox ) {
		describe( "LDEV-3381 debug data keeps the sql with placeholders and the params separate", function() {

			it( title="queries has a sqlRaw column", body=function( currentSpec ) {
				var queries = getQueries();
				expect( listFindNoCase( queries.columnList, "sqlRaw" ) ).toBeGT( 0, "sqlRaw missing, columns: " & queries.columnList );
			});

			it( title="sqlRaw is the sql with ? placeholders, not the values", body=function( currentSpec ) {
				var rows = getMarkerRows( getQueries(), "ldev3381_marker" );
				expect( rows ).toHaveLength( 2 );
				for ( var row in rows ) {
					expect( row.sqlRaw ).toInclude( "?" );
					expect( row.sqlRaw ).notToInclude( "first" );
					expect( row.sqlRaw ).notToInclude( "second" );
				}
			});

			it( title="the same statement with different params has the same sqlRaw but a different sql", body=function( currentSpec ) {
				var rows = getMarkerRows( getQueries(), "ldev3381_marker" );
				expect( rows ).toHaveLength( 2 );
				expect( rows[ 1 ].sqlRaw ).toBe( rows[ 2 ].sqlRaw );
				expect( rows[ 1 ].sql ).notToBe( rows[ 2 ].sql );
				expect( rows[ 1 ].sql ).toInclude( "first" );
				expect( rows[ 2 ].sql ).toInclude( "second" );
			});

			it( title="the params are still available separately", body=function( currentSpec ) {
				var rows = getMarkerRows( getQueries(), "ldev3381_marker" );
				expect( rows ).toHaveLength( 2 );
				expect( rows[ 1 ].paramValue ).toBe( [ "first" ] );
				expect( rows[ 2 ].paramValue ).toBe( [ "second" ] );
				expect( rows[ 1 ].paramType ).toHaveLength( 1 );
			});

			it( title="a query without params has sqlRaw equal to sql", body=function( currentSpec ) {
				var rows = getMarkerRows( getQueries(), "ldev3381_noparam" );
				expect( rows ).toHaveLength( 1 );
				expect( rows[ 1 ].sqlRaw ).toBe( rows[ 1 ].sql );
				expect( rows[ 1 ].sqlRaw ).notToInclude( "?" );
				expect( rows[ 1 ].paramValue ).toHaveLength( 0 );
			});
		});
	}

	private function enableDebug() {
		admin action="updateDebug" type="server" password="#request.SERVERADMINPASSWORD#" debug="true" database="true";
		admin action="PurgeDebugPool" type="server" password="#request.SERVERADMINPASSWORD#";
	}

	private function disableDebug() {
		admin action="updateDebug" type="server" password="#request.SERVERADMINPASSWORD#" debug="false";
		admin action="PurgeDebugPool" type="server" password="#request.SERVERADMINPASSWORD#";
	}

	private query function getQueries() cachedwithin="request" {
		admin action="PurgeDebugPool" type="server" password="#request.SERVERADMINPASSWORD#";
		_InternalRequest( template: "#variables.uri#/index.cfm" );
		var logs = [];
		admin action="getLoggedDebugData" type="server" password="#request.SERVERADMINPASSWORD#" returnVariable="logs";
		for ( var entry in logs ) {
			if ( !isStruct( entry ) || !structKeyExists( entry, "queries" ) ) continue;
			if ( isQuery( entry.queries ) && findNoCase( "ldev3381_", valueList( entry.queries.sql ) ) ) return entry.queries;
		}
		throw( message="no logged debug data with the LDEV-3381 queries found", detail="entries: #arrayLen( logs )#" );
	}

	private array function getMarkerRows( required query queries, required string marker ) {
		var rows = [];
		loop query=arguments.queries {
			if ( findNoCase( arguments.marker, arguments.queries.sql ) ) {
				arrayAppend( rows, {
					sql: arguments.queries.sql,
					sqlRaw: arguments.queries.sqlRaw,
					paramValue: arguments.queries.paramValue,
					paramType: arguments.queries.paramType
				} );
			}
		}
		return rows;
	}

	private string function createURI( string calledName ) {
		var baseURI = "/test/#listLast( getDirectoryFromPath( getCurrentTemplatePath() ), "\/" )#/";
		return baseURI & "" & calledName;
	}

}
