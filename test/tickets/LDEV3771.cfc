component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults, testBox ) {
		describe( "LDEV-3771 multiple exception types in a single catch", function() {

			describe( "script", function() {

				it( title="matches the first alternative", body=function( currentSpec ) {
					expect( scriptCatch( "a" ) ).toBe( "caught:a" );
				});

				it( title="matches a middle alternative", body=function( currentSpec ) {
					expect( scriptCatch( "b" ) ).toBe( "caught:b" );
				});

				it( title="matches the last alternative", body=function( currentSpec ) {
					expect( scriptCatch( "c" ) ).toBe( "caught:c" );
				});

				it( title="does not match, the exception propagates", body=function( currentSpec ) {
					expect( function() {
						try {
							throw type="d" message="boom";
						}
						catch ( a | b | c e ) {
							systemOutput( "must not be reached", true );
						}
					}).toThrow( type="d", regex="boom" );
				});

				it( title="supports dotted type names", body=function( currentSpec ) {
					var result = "";
					try {
						throw type="my.custom.type" message="boom";
					}
					catch ( a.b | my.custom.type e ) {
						result = e.type;
					}
					expect( result ).toBe( "my.custom.type" );
				});

				it( title="supports quoted types and no spaces around the pipe", body=function( currentSpec ) {
					var result = "";
					try {
						throw type="b" message="boom";
					}
					catch ( "a"|"b" e ) {
						result = e.type;
					}
					expect( result ).toBe( "b" );
				});

				it( title="types are matched case insensitive like a single type", body=function( currentSpec ) {
					var result = "";
					try {
						throw type="Beta" message="boom";
					}
					catch ( alpha | beta e ) {
						result = e.type;
					}
					expect( result ).toBe( "Beta" );
				});

				it( title="matches built in exception types and their hierarchy", body=function( currentSpec ) {
					var result = "";
					try {
						var sct = {};
						var x = sct.missing;
					}
					catch ( database | expression e ) {
						result = e.type;
					}
					expect( result ).toBe( "expression" );
				});

				it( title="the first matching catch wins", body=function( currentSpec ) {
					var result = "";
					try {
						throw type="c" message="boom";
					}
					catch ( a | b e ) {
						result = "first";
					}
					catch ( c e ) {
						result = "second";
					}
					catch ( any e ) {
						result = "any";
					}
					expect( result ).toBe( "second" );

					try {
						throw type="b" message="boom";
					}
					catch ( a | b e ) {
						result = "first";
					}
					catch ( any e ) {
						result = "any";
					}
					expect( result ).toBe( "first" );

					try {
						throw type="z" message="boom";
					}
					catch ( a | b e ) {
						result = "first";
					}
					catch ( any e ) {
						result = "any";
					}
					expect( result ).toBe( "any" );
				});

				it( title="finally still runs", body=function( currentSpec ) {
					var result = "";
					try {
						throw type="b" message="boom";
					}
					catch ( a | b e ) {
						result &= "catch,";
					}
					finally {
						result &= "finally";
					}
					expect( result ).toBe( "catch,finally" );
				});

				it( title="a single type and any still work", body=function( currentSpec ) {
					var result = "";
					try {
						throw type="a" message="boom";
					}
					catch ( a e ) {
						result = "single";
					}
					expect( result ).toBe( "single" );
					try {
						throw type="a" message="boom";
					}
					catch ( any e ) {
						result = "any";
					}
					expect( result ).toBe( "any" );
				});

				it( title="any combined with another type is a compile error", body=function( currentSpec ) {
					expect( function() {
						include template="LDEV3771/script-error-any.cfm";
					}).toThrow( regex="cannot be combined" );
					expect( function() {
						include template="LDEV3771/script-error-any-first.cfm";
					}).toThrow( regex="cannot be combined" );
				});

				it( title="an empty alternative is a compile error", body=function( currentSpec ) {
					expect( function() {
						include template="LDEV3771/script-error-empty.cfm";
					}).toThrow( regex="missing the type after the pipe" );
				});

				it( title="a variable name is required", body=function( currentSpec ) {
					expect( function() {
						include template="LDEV3771/script-error-no-name.cfm";
					}).toThrow( regex="variable name is required" );
					expect( function() {
						include template="LDEV3771/script-error-trailing.cfm";
					}).toThrow( regex="variable name is required" );
				});

			});

			describe( "tag", function() {

				it( title="matches the first alternative", body=function( currentSpec ) {
					expect( tagRequest( "tag.cfm", "a" ) ).toBe( "caught:a" );
				});

				it( title="matches a middle alternative", body=function( currentSpec ) {
					expect( tagRequest( "tag.cfm", "b" ) ).toBe( "caught:b" );
				});

				it( title="matches the last alternative", body=function( currentSpec ) {
					expect( tagRequest( "tag.cfm", "c" ) ).toBe( "caught:c" );
				});

				it( title="does not match, the exception propagates", body=function( currentSpec ) {
					expect( tagRequest( "tag-no-match.cfm" ) ).toBe( "propagated:d" );
				});

				it( title="matches built in exception types", body=function( currentSpec ) {
					expect( tagRequest( "tag-hierarchy.cfm" ) ).toBe( "caught:expression" );
				});

				it( title="the first matching cfcatch wins", body=function( currentSpec ) {
					expect( tagRequest( "tag-order.cfm", "a" ) ).toBe( "first:a" );
					expect( tagRequest( "tag-order.cfm", "b" ) ).toBe( "first:b" );
					expect( tagRequest( "tag-order.cfm", "c" ) ).toBe( "second:c" );
					expect( tagRequest( "tag-order.cfm", "z" ) ).toBe( "any:z" );
				});

				it( title="any combined with another type is a compile error", body=function( currentSpec ) {
					expect( function() {
						include template="LDEV3771/tag-error-any.cfm";
					}).toThrow( regex="cannot be combined" );
					expect( function() {
						include template="LDEV3771/tag-error-any-first.cfm";
					}).toThrow( regex="cannot be combined" );
				});

				it( title="an empty alternative is a compile error", body=function( currentSpec ) {
					expect( function() {
						include template="LDEV3771/tag-error-empty.cfm";
					}).toThrow( regex="empty type is not allowed" );
					expect( function() {
						include template="LDEV3771/tag-error-trailing.cfm";
					}).toThrow( regex="empty type is not allowed" );
				});

				it( title="a variable name is required", body=function( currentSpec ) {
					expect( function() {
						include template="LDEV3771/tag-error-no-name.cfm";
					}).toThrow( regex="attribute \[name\] is required" );
				});

			});

		});
	}

	private string function scriptCatch( required string type ) {
		var result = "";
		try {
			throw type=arguments.type message="boom";
		}
		catch ( a | b | c e ) {
			result = "caught:" & e.type;
		}
		return result;
	}

	private string function tagRequest( required string template, string type="" ) {
		var uri = "/test/#listLast( getDirectoryFromPath( getCurrentTemplatePath() ), "\/" )#/LDEV3771/" & arguments.template;
		var result = _InternalRequest(
			template: uri,
			url: len( arguments.type ) ? { type: arguments.type } : {}
		);
		return trim( result.fileContent );
	}

}
