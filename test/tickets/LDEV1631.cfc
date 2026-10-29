component extends="org.lucee.cfml.test.LuceeTestCase" labels="regex,internalRequest" {

	function beforeAll() {
		variables.uri = createURI( "LDEV1631" );
	}

	function run( testResults, testBox ) {
		describe( "LDEV-1631: regex functions can be stopped by the request timeout", function() {

			// the java engine reads its input through charAt() and can be interrupted, the number of
			// steps of the pattern doubles with every additional character
			var fns = [ "reFind", "reFindNoCase", "reMatch", "reMatchNoCase", "reReplace", "reReplaceNoCase", "isValid" ];
			loop array=fns item="local.fn" {
				( function( fn ) {
					it( title="java engine: #fn# with catastrophic backtracking is stopped by the request timeout", body=function( currentSpec ) {
						var start = getTickCount();
						var result = _InternalRequest(
							template: "#variables.uri#/catastrophic.cfm",
							urls: { engine: "java", fn: fn, len: 60 },
							throwonerror: false
						);
						var duration = getTickCount() - start;
						expect( result ).toHaveKey( "error" );
						expect( result.error.message ).toInclude( "timeout" );
						// the timeout is 2 seconds
						expect( duration ).toBeLT( 20000 );
					});
				} )( fn );
			}

			it( title="java engine: the next request is not affected after a regex was stopped", body=function( currentSpec ) {
				var result = _InternalRequest(
					template: "#variables.uri#/results.cfm",
					urls: { engine: "java" }
				);
				expect( isJson( result.filecontent ) ).toBeTrue();
			});

		});

		describe( "LDEV-1631: regex results are unchanged", function() {

			var expected = {
				"reFind": 46,
				"reFindOffset": 27,
				"reFindNoMatch": 0,
				"reFindNoCase": 17,
				"reFindNoCaseOffset": 46,
				"reFindSub": { "pos": [ 46, 46, 50 ], "len": [ 15, 3, 7 ], "match": [ "fox@example.com", "fox", "example" ] },
				"reFindNoCaseSub": { "pos": [ 66, 66, 70 ], "len": [ 15, 3, 7 ], "match": [ "DOG@example.org", "DOG", "example" ] },
				"reFindNoMatchSub": { "pos": [ 0 ], "len": [ 0 ], "match": [ "" ] },
				"reFindAll": [ 13, 18, 27, 42, 47, 59, 78 ],
				"reFindNoCaseAll": [ 41, 66 ],
				"reFindMultiline": 0,
				"reFindMatches": [ "fox@example.com" ],
				"reMatchNoCase": [ "fox@example.com", "DOG@example.org" ],
				"reMatchNone": [],
				"reReplace": "The quick br0wn Fox jumps over the lazy dog, fox@example.com and DOG@example.org",
				"reReplaceAll": "The quick br0wn F0x jumps 0ver the lazy d0g, f0x@example.c0m and DOG@example.0rg",
				"reReplaceNoCase": "The quick brown Fox jumps over the lazy cat, fox@example.com and DOG@example.org",
				"reReplaceNoCaseAll": "The quick brown Fox jumps over the lazy cat, fox@example.com and cat@example.org",
				"reReplaceBackRef": "The quick brown Fox jumps over the lazy dog, example at fox and DOG@example.org",
				"reReplaceNoMatch": "The quick brown Fox jumps over the lazy dog, fox@example.com and DOG@example.org",
				"isValid": true,
				"isNotValid": false,
				"emptyString": ""
			};

			// the perl engine is provided by the Perl Regex extension in this version, its specs live in that extension
			loop list="java" item="local.engine" {
				( function( engine ) {
					it( title="#engine# engine: results of reFind, reMatch, reReplace and isValid (and the NoCase variants) are as expected", body=function( currentSpec ) {
						var result = _InternalRequest(
							template: "#variables.uri#/results.cfm",
							urls: { engine: engine }
						);
						var actual = deserializeJSON( result.filecontent );
						expect( structCount( actual ) ).toBe( structCount( expected ) );
						loop struct=expected index="local.key" item="local.value" {
							expect( actual[ key ] ).toBe( value, key );
						}
					});
				} )( engine );
			}

		});
	}

	private string function createURI( string calledName ) {
		var baseURI = "/test/#listLast( getDirectoryFromPath( getCurrentTemplatePath() ), "\/" )#/";
		return baseURI & "" & calledName;
	}

}
