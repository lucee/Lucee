component extends="org.lucee.cfml.test.LuceeTestCase" labels="query,cfoutput" {

	function run( testResults, testBox ) {
		describe( "LDEV-1791 query group compares the values as strings", function() {

			afterEach( function() {
				structDelete( request, "ldev1791" );
			});

			it( title="cfloop group: 013, 0013 and 00013 are different groups", body=function( currentSpec ) {
				var q = queryNew( "artNr", "varchar", [ [ "013" ], [ "0013" ], [ "00013" ] ] );
				expect( loopGroups( q ) ).toBe( "013,0013,00013" );
				expect( loopGroups( q, true ) ).toBe( "013,0013,00013" );
				expect( loopGroups( q, false ) ).toBe( "013,0013,00013" );
			});

			it( title="cfloop group: identical values still form one group", body=function( currentSpec ) {
				var q = queryNew( "artNr", "varchar", [ [ "013" ], [ "013" ], [ "0013" ], [ "0013" ], [ "013" ] ] );
				expect( loopGroups( q ) ).toBe( "013,0013,013" );
			});

			it( title="cfloop group: values that only look equal as numbers are different groups", body=function( currentSpec ) {
				expect( loopGroups( queryNew( "artNr", "varchar", [ [ "1" ], [ "1.0" ], [ "01" ], [ "1e0" ] ] ) ) ).toBe( "1,1.0,01,1e0" );
				expect( loopGroups( queryNew( "artNr", "varchar", [ [ "10" ], [ "1e1" ], [ "10.00" ] ] ) ) ).toBe( "10,1e1,10.00" );
				expect( loopGroups( queryNew( "artNr", "varchar", [ [ "true" ], [ "yes" ], [ "1" ] ] ) ) ).toBe( "true,yes,1" );
				expect( loopGroups( queryNew( "artNr", "varchar", [ [ "13" ], [ " 13" ], [ "13 " ] ] ) ) ).toBe( "13, 13,13 " );
			});

			it( title="cfloop group: numeric columns still group by the number", body=function( currentSpec ) {
				var q = queryNew( "artNr", "integer", [ [ 13 ], [ 013 ], [ 0013 ], [ 7 ], [ 7 ] ] );
				expect( loopGroups( q ) ).toBe( "13,7" );

				q = queryNew( "artNr", "double", [ [ 1 ], [ 1.0 ], [ 1.5 ], [ 1.50 ] ] );
				expect( loopGroups( q ) ).toBe( "1,1.5" );

				q = queryNew( "artNr", "bigint", [ [ 100 ], [ 100 ], [ 101 ] ] );
				expect( loopGroups( q ) ).toBe( "100,101" );
			});

			it( title="cfloop group: numeric literals with leading zeros are the same number", body=function( currentSpec ) {
				var q = queryNew( "testID,title" );
				queryAddRow( q, 3 );
				querySetCell( q, "testID", 013, 1 );
				querySetCell( q, "testID", 0013, 2 );
				querySetCell( q, "testID", 00013, 3 );
				var count = 0;
				loop query="q" group="testID" {
					count++;
				}
				expect( count ).toBe( 1 );
			});

			it( title="cfloop group: case is ignored by default and when groupCaseSensitive is false", body=function( currentSpec ) {
				var q = queryNew( "artNr", "varchar", [ [ "abc" ], [ "ABC" ], [ "Abc" ], [ "abd" ] ] );
				expect( loopGroups( q ) ).toBe( "abc,abd" );
				expect( loopGroups( q, false ) ).toBe( "abc,abd" );
			});

			it( title="cfloop group: groupCaseSensitive=true keeps the case apart", body=function( currentSpec ) {
				var q = queryNew( "artNr", "varchar", [ [ "abc" ], [ "ABC" ], [ "Abc" ], [ "abd" ] ] );
				expect( loopGroups( q, true ) ).toBe( "abc,ABC,Abc,abd" );
			});

			it( title="cfloop group: empty values", body=function( currentSpec ) {
				var q = queryNew( "artNr", "varchar", [ [ "" ], [ "" ], [ "0" ], [ "" ] ] );
				expect( loopGroups( q ) ).toBe( ",0," );
				expect( loopGroups( q, true ) ).toBe( ",0," );
			});

			it( title="cfoutput group: 013 and 0013 are different groups", body=function( currentSpec ) {
				request.ldev1791 = { q: queryNew( "artNr,id", "varchar,integer", [ [ "013", 1 ], [ "0013", 2 ], [ "00013", 3 ], [ "00013", 4 ] ] ) };
				expect( renderTemplate( "LDEV1791/output.cfm" ) ).toBe( "{013|1;}{0013|2;}{00013|3;4;}" );
			});

			it( title="cfoutput group: numeric column still groups by the number", body=function( currentSpec ) {
				request.ldev1791 = { q: queryNew( "artNr,id", "integer,integer", [ [ 13, 1 ], [ 013, 2 ], [ 7, 3 ] ] ) };
				expect( renderTemplate( "LDEV1791/output.cfm" ) ).toBe( "{13|1;2;}{7|3;}" );
			});

			it( title="cfoutput group: groupCaseSensitive on and off", body=function( currentSpec ) {
				var q = queryNew( "artNr,id", "varchar,integer", [ [ "abc", 1 ], [ "ABC", 2 ], [ "013", 3 ], [ "0013", 4 ] ] );
				request.ldev1791 = { q: q, cs: false };
				expect( renderTemplate( "LDEV1791/outputCase.cfm" ) ).toBe( "{abc|1;2;}{013|3;}{0013|4;}" );
				expect( renderTemplate( "LDEV1791/output.cfm" ) ).toBe( "{abc|1;2;}{013|3;}{0013|4;}" );
				request.ldev1791.cs = true;
				expect( renderTemplate( "LDEV1791/outputCase.cfm" ) ).toBe( "{abc|1;}{ABC|2;}{013|3;}{0013|4;}" );
			});

			it( title="nested cfoutput group: inner group compares as strings", body=function( currentSpec ) {
				var q = queryNew( "cat,artNr", "varchar,varchar", [ [ "a", "013" ], [ "a", "0013" ], [ "a", "0013" ], [ "b", "13" ], [ "b", "013" ] ] );
				request.ldev1791 = { q: q, cs: false };
				expect( renderTemplate( "LDEV1791/outputNested.cfm" ) ).toBe( "[a/013][a/0013][b/13][b/013]" );
				request.ldev1791.cs = true;
				expect( renderTemplate( "LDEV1791/outputNested.cfm" ) ).toBe( "[a/013][a/0013][b/13][b/013]" );
			});

			it( title="nested cfoutput group: groupCaseSensitive", body=function( currentSpec ) {
				var q = queryNew( "cat,artNr", "varchar,varchar", [ [ "a", "x" ], [ "a", "X" ] ] );
				request.ldev1791 = { q: q, cs: false };
				expect( renderTemplate( "LDEV1791/outputNested.cfm" ) ).toBe( "[a/x]" );
				request.ldev1791.cs = true;
				expect( renderTemplate( "LDEV1791/outputNested.cfm" ) ).toBe( "[a/x][a/X]" );
			});

			it( title="nested cfloop group: inner group compares as strings", body=function( currentSpec ) {
				var q = queryNew( "cat,artNr", "varchar,varchar", [ [ "a", "013" ], [ "a", "0013" ], [ "a", "0013" ], [ "b", "13" ], [ "b", "013" ] ] );
				request.ldev1791 = { q: q, cs: false };
				expect( renderTemplate( "LDEV1791/loopNested.cfm" ) ).toBe( "[a/013][a/0013][b/13][b/013]" );
				q = queryNew( "cat,artNr", "varchar,varchar", [ [ "a", "x" ], [ "a", "X" ] ] );
				request.ldev1791 = { q: q, cs: false };
				expect( renderTemplate( "LDEV1791/loopNested.cfm" ) ).toBe( "[a/x]" );
				request.ldev1791.cs = true;
				expect( renderTemplate( "LDEV1791/loopNested.cfm" ) ).toBe( "[a/x][a/X]" );
			});

		});
	}

	private string function loopGroups( required query q, boolean groupCaseSensitive ) {
		var result = [];
		var qq = arguments.q;
		if ( isNull( arguments.groupCaseSensitive ) ) {
			loop query="qq" group="artNr" {
				arrayAppend( result, qq.artNr );
			}
		}
		else {
			var cs = arguments.groupCaseSensitive;
			loop query="qq" group="artNr" groupCaseSensitive=cs {
				arrayAppend( result, qq.artNr );
			}
		}
		return arrayToList( result );
	}

	private string function renderTemplate( required string template ) {
		var result = "";
		savecontent variable="result" {
			include template=arguments.template;
		}
		return trim( result );
	}

}
