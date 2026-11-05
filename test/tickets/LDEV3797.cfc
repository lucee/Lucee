component extends="org.lucee.cfml.test.LuceeTestCase" {

	function beforeAll() {
		variables.uri = createURI( "LDEV3797/id.cfm" );
	}

	function run( testResults, testBox ) {
		describe( "LDEV-3797 getRequestId()", function() {

			it( "returns a 22 character URL-safe Base64 UUID", function() {
				assertRequestUuid( getRequestId() );
			});

			it( "is stable for the life of the request", function() {
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
				expect( pair.first ).toBe( parentId );
				expect( pair.second ).toBe( parentId );
			});

			it( "is shared by threads of the same request and stable under concurrent calls", function() {
				var parentId = getRequestId();
				var names = [];
				loop from="1" to="8" index="local.i" {
					var tn = "ldev3797_#createUUID()#";
					arrayAppend( names, tn );
					thread name=tn {
						var seen = getRequestId();
						loop from="1" to="20" index="local.n" {
							if ( getRequestId() != seen ) seen = "";
						}
						thread.requestId = seen;
					}
				}
				thread action="join" name=arrayToList( names ) timeout=10000;
				loop array=names item="local.tn" {
					expect( cfthread[ tn ].status ).toBe( "COMPLETED", "thread #tn# did not complete: #serializeJSON( cfthread[ tn ] )#" );
					expect( cfthread[ tn ].requestId ).toBe( parentId );
				}
				expect( getRequestId() ).toBe( parentId );
			});

			it( "differs between requests, including a reused page context", function() {
				var seen = {};
				seen[ getRequestId() ] = true;
				loop from="1" to="5" index="local.i" {
					var result = _internalRequest( template: variables.uri );
					var pair = parsePair( result.filecontent );
					expect( pair.first ).toBe( pair.second, "id changed inside one internal request: [#result.filecontent#]" );
					assertRequestUuid( pair.first );
					expect( structKeyExists( seen, pair.first ) ).toBeFalse( "request id was reused: #pair.first#" );
					seen[ pair.first ] = true;
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
				expect( data.returnType ).toBe( "string" );
				expect( data.status ).toBe( "implemented" );
				expect( data.type ).toBe( "java" );
				expect( data.argMin ).toBe( 0 );
				// no-arg functions leave argMax at the library default; the compiler uses the empty argument list
				expect( data.argMax ).toBe( -1 );
				expect( arrayLen( data.arguments ) ).toBe( 0 );
				expect( data.introduced ).toBe( "9.0.0.0" );
				expect( arrayContains( data.keywords, "request" ) ).toBeTrue();
				expect( data.description ).toInclude( "Base64" );
				expect( data.description ).toInclude( "request" );
			});

		});
	}

	private void function assertRequestUuid( required string id ) {
		expect( len( arguments.id ) ).toBe( 22 );
		expect( reFind( "^[A-Za-z0-9_-]{22}$", arguments.id ) ).toBe( 1 );
		var standard = replace( replace( arguments.id, "-", "+", "all" ), "_", "/", "all" ) & "==";
		expect( len( binaryDecode( standard, "base64" ) ) ).toBe( 16 );
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
