component extends="org.lucee.cfml.test.LuceeTestCase" {

	function beforeAll() {
		variables.uri = createURI( "LDEV3797/id.cfm" );
	}

	function run( testResults, testBox ) {
		describe( "LDEV-3797 getRequestId()", function() {

			it( "returns the page context request id", function() {
				var id = getRequestId();
				expect( isNumeric( id ) ).toBeTrue();
				expect( isValid( "integer", id ) ).toBeTrue();
				expect( id ).toBe( getPageContext().getRequestId() );
			});

			it( "is stable for the life of the page context", function() {
				var first = getRequestId();
				var second = getRequestId();
				expect( second ).toBe( first );
				loop from="1" to="50" index="local.i" {
					expect( getRequestId() ).toBe( first );
				}
			});

			it( "is the same in an included template", function() {
				var parentId = getRequestId();
				savecontent variable="local.out" {
					include template="LDEV3797/id.cfm";
				}
				var pair = parsePair( out );
				expect( int( pair.first ) ).toBe( int( parentId ) );
				expect( int( pair.second ) ).toBe( int( parentId ) );
			});

			it( "gives a cloned thread its own stable id and leaves the caller unchanged", function() {
				var parentId = getRequestId();
				var names = [];
				loop from="1" to="4" index="local.i" {
					var tn = "ldev3797_#createUUID()#";
					arrayAppend( names, tn );
					thread name=tn callerId=parentId {
						var first = getRequestId();
						var second = getRequestId();
						thread.first = first;
						thread.second = second;
						thread.callerId = attributes.callerId;
					}
				}
				thread action="join" name=arrayToList( names ) timeout=10000;
				loop array=names item="local.tn" {
					expect( cfthread[ tn ].status ).toBe( "COMPLETED", "thread #tn# did not complete: #serializeJSON( cfthread[ tn ] )#" );
					expect( int( cfthread[ tn ].first ) ).toBe( int( cfthread[ tn ].second ) );
					expect( int( cfthread[ tn ].first ) ).notToBe( int( cfthread[ tn ].callerId ) );
				}
				expect( getRequestId() ).toBe( parentId );
			});

			it( "differs between requests, including a reused page context", function() {
				var seen = {};
				seen[ int( getRequestId() ) ] = true;
				loop from="1" to="5" index="local.i" {
					var result = _internalRequest( template: variables.uri );
					var pair = parsePair( result.filecontent );
					expect( int( pair.first ) ).toBe( int( pair.second ), "id changed inside one internal request: [#result.filecontent#]" );
					expect( isValid( "integer", pair.first ) ).toBeTrue( "not an integer id: [#pair.first#]" );
					expect( structKeyExists( seen, int( pair.first ) ) ).toBeFalse( "request id was reused: #pair.first#" );
					seen[ int( pair.first ) ] = true;
				}
			});

			it( "rejects arguments", function() {
				expect( function() {
					evaluate( 'getRequestId( "nope" )' );
				} ).toThrow();
				var fn = getBuiltInFunction( "getRequestId" );
				expect( function() {
					fn( "nope" );
				} ).toThrow();
			});

			it( "exposes function metadata", function() {
				expect( structKeyExists( getFunctionList(), "getrequestid" ) ).toBeTrue();
				var data = getFunctionData( "getRequestId" );
				expect( data.name ).toBe( "getrequestid" );
				expect( data.nameWithCase ).toBe( "getRequestId" );
				expect( data.returnType ).toBe( "number" );
				expect( data.status ).toBe( "implemented" );
				expect( data.type ).toBe( "java" );
				expect( data.argMin ).toBe( 0 );
				// no-arg functions leave argMax at the library default; the compiler uses the empty argument list
				expect( data.argMax ).toBe( -1 );
				expect( arrayLen( data.arguments ) ).toBe( 0 );
				expect( data.introduced ).toBe( "9.0.0.0" );
				expect( arrayContains( data.keywords, "request" ) ).toBeTrue();
				expect( data.description ).toInclude( "integer" );
				expect( data.description ).toInclude( "page context" );
				expect( data.description ).notToInclude( "Base64" );
				expect( data.description ).notToInclude( "UUID" );
			});

		});
	}

	private struct function parsePair( required string content ) {
		var parts = listToArray( trim( arguments.content ), "|" );
		expect( arrayLen( parts ) ).toBe( 2, "unexpected request id output [#arguments.content#]" );
		return { first: parts[ 1 ], second: parts[ 2 ] };
	}

	private string function createURI( string calledName ) {
		var baseURI = "/test/#listLast( getDirectoryFromPath( getCurrentTemplatePath() ), "\/" )#/";
		return baseURI & calledName;
	}

}
