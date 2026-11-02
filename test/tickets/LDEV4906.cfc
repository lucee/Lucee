component extends="org.lucee.cfml.test.LuceeTestCase" labels="list" {

	variables.sentinel = chr( 1 );

	variables.lists = [ "a::b::c", "a::::b::", "::a::b", "a:b::c", "", "a::A::b::a", "x", "a:::b", "::", "b::b::b" ];
	variables.delimiter = "::";

	function run( testResults, testBox ) {

		describe( "LDEV-4906 multiCharacterDelimiter for the list functions", function() {

			it( title="listInsertAt() inserts the whole delimiter (ticket example)", body=function( currentSpec ) {
				expect( listInsertAt( "1, 3", 2, 2, ", ", false, true ) ).toBe( "1, 2, 3" );
				expect( listInsertAt( "1, 3", 2, 2, ", ", true, true ) ).toBe( "1, 2, 3" );
				expect( listInsertAt( "1, 2, 4", 3, 3, ", ", false, true ) ).toBe( "1, 2, 3, 4" );
				expect( listInsertAt( "a::c", 1, "x", "::", false, true ) ).toBe( "x::a::c" );
				expect( listInsertAt( "a::c", 2, "x::y", "::", false, true ) ).toBe( "a::x::y::c" );
				expect( listInsertAt( "1, 3", 2, 2, ", ", false, true ) ).toBe( "1, 2, 3" );
				expect( "1, 3".listInsertAt( 2, 2, ", ", false, true ) ).toBe( "1, 2, 3" );
				expect( listInsertAt( list="1, 3", position=2, value=2, delimiters=", ", multiCharacterDelimiter=true ) ).toBe( "1, 2, 3" );
			});

			it( title="listSetAt() keeps the whole delimiter", body=function( currentSpec ) {
				expect( listSetAt( "1, 2, 3", 2, "x", ", ", false, true ) ).toBe( "1, x, 3" );
				expect( listSetAt( "a::b::c", 3, "x::y", "::", false, true ) ).toBe( "a::b::x::y" );
				expect( listSetAt( "a::::b", 2, "x", "::", true, true ) ).toBe( "a::x::b" );
				expect( "1, 2, 3".listSetAt( 2, "x", ", ", false, true ) ).toBe( "1, x, 3" );
			});

			it( title="listPrepend() uses the whole delimiter", body=function( currentSpec ) {
				expect( listPrepend( "2, 3", 1, ", ", true, true ) ).toBe( "1, 2, 3" );
				expect( listPrepend( "", 1, ", ", true, true ) ).toBe( "1" );
				expect( listPrepend( "b::::c::", "a", "::", false, true ) ).toBe( "a::b::c" );
				expect( listPrepend( "b::c", "a::", "::", true, true ) ).toBe( "a::::b::c" );
				expect( listPrepend( "::", "a", "::", false, true ) ).toBe( "a" );
				expect( listPrepend( "::", "a", "::", true, true ) ).toBe( "a::::" );
				expect( "2, 3".listPrepend( 1, ", ", true, true ) ).toBe( "1, 2, 3" );
				expect( listPrepend( list="b::c", value="a", delimiters="::", multiCharacterDelimiter=true ) ).toBe( "a::b::c" );
			});

			it( title="listDeleteAt()", body=function( currentSpec ) {
				expect( listDeleteAt( "1, 2, 3", 2, ", ", false, true ) ).toBe( "1, 3" );
				expect( listDeleteAt( "a::b::c", 1, "::", false, true ) ).toBe( "b::c" );
				expect( listDeleteAt( "a::b::c", 3, "::", false, true ) ).toBe( "a::b" );
				expect( listDeleteAt( "a:b::c", 1, "::", false, true ) ).toBe( "c" );
				expect( "a::b::c".listDeleteAt( 2, "::", false, true ) ).toBe( "a::c" );
			});

			it( title="listGetAt() / listFirst() / listLast() / listRest()", body=function( currentSpec ) {
				expect( listGetAt( "a::b::c", 2, "::", false, true ) ).toBe( "b" );
				expect( listGetAt( "a:b::c", 1, "::", false, true ) ).toBe( "a:b" );
				expect( listFirst( "a::b::c", "::", false, 1, true ) ).toBe( "a" );
				expect( listFirst( "a::b::c", "::", false, 2, true ) ).toBe( "a::b" );
				expect( listFirst( "a:b::c", "::", false, 1, true ) ).toBe( "a:b" );
				expect( listLast( "a::b::c", "::", false, 1, true ) ).toBe( "c" );
				expect( listLast( "a::b::c", "::", false, 2, true ) ).toBe( "b::c" );
				expect( listLast( "a::b:c", "::", false, 1, true ) ).toBe( "b:c" );
				expect( listRest( "a::b::c", "::", false, 1, true ) ).toBe( "b::c" );
				expect( listRest( "a::b::c", "::", false, 2, true ) ).toBe( "c" );
				expect( listRest( "a:b::c", "::", false, 1, true ) ).toBe( "c" );
				expect( "a::b::c".listGetAt( 2, "::", false, true ) ).toBe( "b" );
				expect( "a::b::c".listFirst( "::", false, 1, true ) ).toBe( "a" );
				expect( "a::b::c".listLast( "::", false, 1, true ) ).toBe( "c" );
				expect( "a::b::c".listRest( "::", false, 1, true ) ).toBe( "b::c" );
			});

			it( title="listLen() / listIndexExists() / listFind() / listFindNoCase() / listValueCount() / listValueCountNoCase()", body=function( currentSpec ) {
				expect( listLen( "a::b::c", "::", false, true ) ).toBe( 3 );
				expect( listLen( "a:b::c", "::", false, true ) ).toBe( 2 );
				expect( listLen( "a::::b::", "::", false, true ) ).toBe( 2 );
				expect( listLen( "a::::b::", "::", true, true ) ).toBe( 4 );
				expect( listIndexExists( "a::b::c", 3, "::", false, true ) ).toBeTrue();
				expect( listIndexExists( "a:b::c", 3, "::", false, true ) ).toBeFalse();
				expect( listFind( "a::b::c", "b", "::", false, true ) ).toBe( 2 );
				expect( listFind( "a:b::c", "b", "::", false, true ) ).toBe( 0 );
				expect( listFind( "a:b::c", "a:b", "::", false, true ) ).toBe( 1 );
				expect( listFindNoCase( "a::b::c", "B", "::", false, true ) ).toBe( 2 );
				expect( listFindNoCase( "a:b::c", "A:B", "::", false, true ) ).toBe( 1 );
				expect( listValueCount( "a::b::a", "a", "::", false, true ) ).toBe( 2 );
				expect( listValueCount( "a::b::A", "a", "::", false, true ) ).toBe( 1 );
				expect( listValueCountNoCase( "a::b::A", "a", "::", false, true ) ).toBe( 2 );
				expect( listValueCountNoCase( "a:b::A:B", "a:b", "::", false, true ) ).toBe( 2 );
				expect( "a::b::c".listLen( "::", false, true ) ).toBe( 3 );
				expect( "a::b::c".listIndexExists( 3, "::", false, true ) ).toBeTrue();
				expect( "a::b::c".listFind( "b", "::", false, true ) ).toBe( 2 );
				expect( "a::b::c".listFindNoCase( "B", "::", false, true ) ).toBe( 2 );
				expect( "a::b::a".listValueCount( "a", "::", false, true ) ).toBe( 2 );
				expect( "a::b::A".listValueCountNoCase( "a", "::", false, true ) ).toBe( 2 );
			});

			it( title="listItemTrim() / listSort() / listQualify()", body=function( currentSpec ) {
				expect( listItemTrim( "a :: b :: c", "::", false, true ) ).toBe( "a::b::c" );
				expect( listItemTrim( "a :: b :: c", "::", true, true ) ).toBe( "a::b::c" );
				expect( listSort( "c::a::b", "text", "asc", "::", false, true ) ).toBe( "a::b::c" );
				expect( listSort( "c::a::b", "text", "desc", "::", false, true ) ).toBe( "c::b::a" );
				expect( listSort( "10::9::100", "numeric", "asc", "::", false, true ) ).toBe( "9::10::100" );
				expect( listQualify( "a::b::c", "'", "::", "all", false, true ) ).toBe( "'a'::'b'::'c'" );
				expect( listQualify( "a:b::c", "'", "::", "all", false, true ) ).toBe( "'a:b'::'c'" );
				expect( "a :: b :: c".listItemTrim( "::", false, true ) ).toBe( "a::b::c" );
				expect( "c::a::b".listSort( "text", "asc", "::", false, true ) ).toBe( "a::b::c" );
				expect( "a::b::c".listQualify( "'", "::", "all", false, true ) ).toBe( "'a'::'b'::'c'" );
			});

			it( title="listGetDuplicates() / listRemoveDuplicates()", body=function( currentSpec ) {
				expect( listGetDuplicates( "a::b::a", "::", false, false, true ) ).toBe( "a" );
				expect( listGetDuplicates( "a::b::A", "::", true, false, true ) ).toBe( "A" );
				expect( listGetDuplicates( "a::b::A", "::", false, false, true ) ).toBe( "" );
				expect( listRemoveDuplicates( "a::b::a", "::", false, true ) ).toBe( "a::b" );
				expect( listRemoveDuplicates( "a::b::A", "::", true, true ) ).toBe( "a::b" );
				expect( listRemoveDuplicates( "a:b::a:b::c", "::", false, true ) ).toBe( "a:b::c" );
				expect( "a::b::a".listGetDuplicates( "::", false, false, true ) ).toBe( "a" );
				expect( "a::b::a".listRemoveDuplicates( "::", false, true ) ).toBe( "a::b" );
			});

			it( title="multiCharacterDelimiter=true with a single character delimiter behaves like the default", body=function( currentSpec ) {
				expect( listInsertAt( "1,3", 2, 2, ",", false, true ) ).toBe( listInsertAt( "1,3", 2, 2, "," ) );
				expect( listSetAt( "1,2,3", 2, "x", ",", false, true ) ).toBe( listSetAt( "1,2,3", 2, "x", "," ) );
				expect( listPrepend( "2,3", 1, ",", false, true ) ).toBe( listPrepend( "2,3", 1, ",", false ) );
				expect( listDeleteAt( "1,2,3", 2, ",", false, true ) ).toBe( "1,3" );
				expect( listLen( "1,2,3", ",", false, true ) ).toBe( 3 );
				expect( listSort( "c,a,b", "text", "asc", ",", false, true ) ).toBe( "a,b,c" );
				expect( listQualify( "a,b", "'", ",", "all", false, true ) ).toBe( "'a','b'" );
			});

			it( title="default (multiCharacterDelimiter not set or false) is unchanged: every character is a delimiter", body=function( currentSpec ) {
				expect( listLen( "a:b::c", "::" ) ).toBe( 3 );
				expect( listLen( "a:b::c", "::", false, false ) ).toBe( 3 );
				expect( listLen( "a:b::c", "::", true ) ).toBe( 4 );
				expect( listFind( "a:b::c", "b", "::" ) ).toBe( 2 );
				expect( listFind( "a:b::c", "b", "::", false, false ) ).toBe( 2 );
				expect( listFindNoCase( "a:b::c", "B", "::" ) ).toBe( 2 );
				expect( listValueCount( "a:b::a", "a", "::" ) ).toBe( 2 );
				expect( listValueCountNoCase( "a:b::A", "a", "::" ) ).toBe( 2 );
				expect( listIndexExists( "a:b::c", 3, "::" ) ).toBeTrue();
				expect( listGetAt( "a:b::c", 3, "::" ) ).toBe( "c" );
				expect( listGetAt( "a:b::c", 3, "::", false, false ) ).toBe( "c" );
				expect( listFirst( "a:b::c", "::" ) ).toBe( "a" );
				expect( listFirst( "a:b::c", "::", false, 1, false ) ).toBe( "a" );
				expect( listLast( "a:b::c", "::" ) ).toBe( "c" );
				expect( listLast( "a:b::c", "::", false, 1, false ) ).toBe( "c" );
				expect( listRest( "a:b::c", "::", false, 1, false ) ).toBe( listRest( "a:b::c", "::" ) );
				expect( listInsertAt( "a,c", 2, "b" ) ).toBe( "a,b,c" );
				expect( listInsertAt( "a,c", 2, "b", "," ) ).toBe( "a,b,c" );
				expect( listInsertAt( "a,c", 2, "b", ",", false, false ) ).toBe( "a,b,c" );
				expect( listInsertAt( "1, 3", 2, 2, ", ", false, false ) ).toBe( listInsertAt( "1, 3", 2, 2, ", " ) );
				expect( listSetAt( "a,x,c", 2, "b" ) ).toBe( "a,b,c" );
				expect( listSetAt( "1, 2, 3", 2, "x", ", ", false, false ) ).toBe( listSetAt( "1, 2, 3", 2, "x", ", " ) );
				expect( listPrepend( "b,c", "a" ) ).toBe( "a,b,c" );
				expect( listPrepend( "2, 3", 1, ", ", true, false ) ).toBe( listPrepend( "2, 3", 1, ", ", true ) );
				expect( listDeleteAt( "a,x,c", 2 ) ).toBe( "a,c" );
				expect( listDeleteAt( "1, 2, 3", 2, ", ", false, false ) ).toBe( listDeleteAt( "1, 2, 3", 2, ", " ) );
				expect( listQualify( "a,b", "'" ) ).toBe( "'a','b'" );
				expect( listQualify( "a::b", "'", "::", "all", false, false ) ).toBe( listQualify( "a::b", "'", "::", "all", false ) );
				expect( listSort( "c,a,b", "text" ) ).toBe( "a,b,c" );
				expect( listSort( "c::a::b", "text", "asc", "::", false, false ) ).toBe( listSort( "c::a::b", "text", "asc", "::" ) );
				expect( listRemoveDuplicates( "a,b,a" ) ).toBe( "a,b" );
				expect( listRemoveDuplicates( "a::b::a", "::", false, false ) ).toBe( listRemoveDuplicates( "a::b::a", "::" ) );
				expect( listGetDuplicates( "a,b,a" ) ).toBe( "a" );
				expect( listGetDuplicates( "a::b::a", "::", false, false, false ) ).toBe( listGetDuplicates( "a::b::a", "::" ) );
				expect( listItemTrim( " a , b " ) ).toBe( "a,b" );
				expect( listItemTrim( " a ::b", "::", false, false ) ).toBe( listItemTrim( " a ::b", "::" ) );
				expect( listRest( "a,b,c" ) ).toBe( "b,c" );
			});

			it( title="the delimiter is only used as a whole, an overlapping occurrence is found left to right", body=function( currentSpec ) {
				expect( listLen( "a:::b", "::", false, true ) ).toBe( 2 );
				expect( listGetAt( "a:::b", 2, "::", false, true ) ).toBe( ":b" );
				expect( listDeleteAt( "a:::b", 1, "::", false, true ) ).toBe( ":b" );
			});

			it( title="characters of the Unicode private use area and the sentinel character in the input are handled", body=function( currentSpec ) {
				var pua = chr( 57344 ) & "x" & chr( 57345 );
				expect( listGetAt( "a::" & pua & "::c", 2, "::", false, true ) ).toBe( pua );
				expect( listInsertAt( "a::c", 2, pua, "::", false, true ) ).toBe( "a::" & pua & "::c" );
				expect( listSetAt( "a::" & pua & "::c", 3, chr( 57346 ), "::", false, true ) ).toBe( "a::" & pua & "::" & chr( 57346 ) );
				expect( listLen( "a::" & pua & "::c", "::", false, true ) ).toBe( 3 );
				expect( listSort( "c::" & pua & "::a", "text", "asc", "::", false, true ) ).toBe( "a::c::" & pua );
				expect( listPrepend( pua, "a", "::", true, true ) ).toBe( "a::" & pua );
			});

			it( title="every list function gives the same result as with a single character delimiter (all modes)", body=function( currentSpec ) {
				var fns = getFunctions();
				loop array=variables.lists item="local.list" {
					loop list="false,true" item="local.empty" {
						loop struct=fns index="local.name" item="local.fn" {
							var masked = replace( list, variables.delimiter, variables.sentinel, "all" );
							var args = fn.args( list, local.empty );
							var refArgs = fn.refArgs( masked, local.empty );
							if ( !fn.valid( masked, local.empty ) ) continue;
							var expected = fn.ref( refArgs );
							if ( fn.string ) expected = replace( expected, variables.sentinel, variables.delimiter, "all" );
							var actual = fn.multi( args );
							expect( actual ).toBe( expected, "#name# list=[#list#] includeEmptyFields=#local.empty#" );
						}
					}
				}
			});

			it( title="named arguments", body=function( currentSpec ) {
				expect( listLen( list="a::b", delimiters="::", multiCharacterDelimiter=true ) ).toBe( 2 );
				expect( listFirst( list="a::b", delimiters="::", multiCharacterDelimiter=true ) ).toBe( "a" );
				expect( listLast( list="a::b", delimiters="::", multiCharacterDelimiter=true ) ).toBe( "b" );
				expect( listRest( list="a::b::c", delimiters="::", multiCharacterDelimiter=true ) ).toBe( "b::c" );
				expect( listGetAt( list="a::b", position=2, delimiters="::", multiCharacterDelimiter=true ) ).toBe( "b" );
				expect( listDeleteAt( list="a::b", position=1, delimiters="::", multiCharacterDelimiter=true ) ).toBe( "b" );
				expect( listSetAt( list="a::b", position=1, value="x", delimiters="::", multiCharacterDelimiter=true ) ).toBe( "x::b" );
				expect( listFind( list="a::b", value="b", delimiters="::", multiCharacterDelimiter=true ) ).toBe( 2 );
				expect( listSort( list="b::a", sort_type="text", delimiters="::", multiCharacterDelimiter=true ) ).toBe( "a::b" );
				expect( listQualify( list="a::b", qualifier="'", delimiters="::", multiCharacterDelimiter=true ) ).toBe( "'a'::'b'" );
				expect( listRemoveDuplicates( list="a::b::a", delimiters="::", multiCharacterDelimiter=true ) ).toBe( "a::b" );
				expect( listItemTrim( list=" a :: b ", delimiters="::", multiCharacterDelimiter=true ) ).toBe( "a::b" );
			});

			it( title="listQualify() with multiCharacterDelimiter inside a cfquery (PreserveSingleQuotes handling)", body=function( currentSpec ) {
				var result = "";
				savecontent variable="result" {
					include template="LDEV4906/query.cfm";
				}
				// the multi character delimiter is kept as one, "a b" is one element, the single quote of "it's" is escaped
				// without multiCharacterDelimiter (last query) the space is a delimiter as well, so "a" is an element
				expect( trim( result ) ).toBe( "1|0|1|1" );
			});

		});
	}

	private struct function getFunctions() {
		var d = variables.delimiter;
		var s = variables.sentinel;
		var fns = {};

		// every entry: args(list,empty) -> arguments of the multi call, refArgs(maskedList,empty) -> arguments of the reference (single character) call
		fns.listLen = {
			string: false,
			valid: function( l, e ) { return true; },
			args: function( l, e ) { return [ l, e ]; },
			refArgs: function( l, e ) { return [ l, e ]; },
			multi: function( a ) { return listLen( a[ 1 ], variables.delimiter, a[ 2 ], true ); },
			ref: function( a ) { return listLen( a[ 1 ], variables.sentinel, a[ 2 ] ); }
		};
		fns.listFirst = {
			string: true,
			valid: function( l, e ) { return listLen( l, variables.sentinel, e ) > 0; },
			args: function( l, e ) { return [ l, e ]; },
			refArgs: function( l, e ) { return [ l, e ]; },
			multi: function( a ) { return listFirst( a[ 1 ], variables.delimiter, a[ 2 ], 2, true ); },
			ref: function( a ) { return listFirst( a[ 1 ], variables.sentinel, a[ 2 ], 2 ); }
		};
		fns.listLast = {
			string: true,
			valid: function( l, e ) { return listLen( l, variables.sentinel, e ) > 0; },
			args: function( l, e ) { return [ l, e ]; },
			refArgs: function( l, e ) { return [ l, e ]; },
			multi: function( a ) { return listLast( a[ 1 ], variables.delimiter, a[ 2 ], 2, true ); },
			ref: function( a ) { return listLast( a[ 1 ], variables.sentinel, a[ 2 ], 2 ); }
		};
		fns.listRest = {
			string: true,
			valid: function( l, e ) { return listLen( l, variables.sentinel, e ) > 0; },
			args: function( l, e ) { return [ l, e ]; },
			refArgs: function( l, e ) { return [ l, e ]; },
			multi: function( a ) { return listRest( a[ 1 ], variables.delimiter, a[ 2 ], 1, true ); },
			ref: function( a ) { return listRest( a[ 1 ], variables.sentinel, a[ 2 ], 1 ); }
		};
		fns.listGetAt = {
			string: true,
			valid: function( l, e ) { return listLen( l, variables.sentinel, e ) >= 2; },
			args: function( l, e ) { return [ l, e ]; },
			refArgs: function( l, e ) { return [ l, e ]; },
			multi: function( a ) { return listGetAt( a[ 1 ], 2, variables.delimiter, a[ 2 ], true ); },
			ref: function( a ) { return listGetAt( a[ 1 ], 2, variables.sentinel, a[ 2 ] ); }
		};
		fns.listDeleteAt = {
			string: true,
			valid: function( l, e ) { return listLen( l, variables.sentinel, e ) >= 2; },
			args: function( l, e ) { return [ l, e ]; },
			refArgs: function( l, e ) { return [ l, e ]; },
			multi: function( a ) { return listDeleteAt( a[ 1 ], 2, variables.delimiter, a[ 2 ], true ); },
			ref: function( a ) { return listDeleteAt( a[ 1 ], 2, variables.sentinel, a[ 2 ] ); }
		};
		fns.listSetAt = {
			string: true,
			valid: function( l, e ) { return listLen( l, variables.sentinel, e ) >= 2; },
			args: function( l, e ) { return [ l, e ]; },
			refArgs: function( l, e ) { return [ l, e ]; },
			multi: function( a ) { return listSetAt( a[ 1 ], 2, "X" & variables.delimiter & "Y", variables.delimiter, a[ 2 ], true ); },
			ref: function( a ) { return listSetAt( a[ 1 ], 2, "X" & variables.sentinel & "Y", variables.sentinel, a[ 2 ] ); }
		};
		fns.listInsertAt = {
			string: true,
			valid: function( l, e ) { return listLen( l, variables.sentinel, e ) >= 1; },
			args: function( l, e ) { return [ l, e ]; },
			refArgs: function( l, e ) { return [ l, e ]; },
			multi: function( a ) { return listInsertAt( a[ 1 ], 1, "X" & variables.delimiter & "Y", variables.delimiter, a[ 2 ], true ); },
			ref: function( a ) { return listInsertAt( a[ 1 ], 1, "X" & variables.sentinel & "Y", variables.sentinel, a[ 2 ] ); }
		};
		fns.listInsertAtLast = {
			string: true,
			valid: function( l, e ) { return listLen( l, variables.sentinel, e ) >= 1; },
			args: function( l, e ) { return [ l, e ]; },
			refArgs: function( l, e ) { return [ l, e ]; },
			multi: function( a ) { return listInsertAt( a[ 1 ], listLen( a[ 1 ], variables.delimiter, a[ 2 ], true ), "Z", variables.delimiter, a[ 2 ], true ); },
			ref: function( a ) { return listInsertAt( a[ 1 ], listLen( a[ 1 ], variables.sentinel, a[ 2 ] ), "Z", variables.sentinel, a[ 2 ] ); }
		};
		fns.listIndexExists = {
			string: false,
			valid: function( l, e ) { return true; },
			args: function( l, e ) { return [ l, e ]; },
			refArgs: function( l, e ) { return [ l, e ]; },
			multi: function( a ) { return listIndexExists( a[ 1 ], 2, variables.delimiter, a[ 2 ], true ); },
			ref: function( a ) { return listIndexExists( a[ 1 ], 2, variables.sentinel, a[ 2 ] ); }
		};
		fns.listFind = {
			string: false,
			valid: function( l, e ) { return true; },
			args: function( l, e ) { return [ l, e ]; },
			refArgs: function( l, e ) { return [ l, e ]; },
			multi: function( a ) { return listFind( a[ 1 ], "b", variables.delimiter, a[ 2 ], true ); },
			ref: function( a ) { return listFind( a[ 1 ], "b", variables.sentinel, a[ 2 ] ); }
		};
		fns.listFindNoCase = {
			string: false,
			valid: function( l, e ) { return true; },
			args: function( l, e ) { return [ l, e ]; },
			refArgs: function( l, e ) { return [ l, e ]; },
			multi: function( a ) { return listFindNoCase( a[ 1 ], "A", variables.delimiter, a[ 2 ], true ); },
			ref: function( a ) { return listFindNoCase( a[ 1 ], "A", variables.sentinel, a[ 2 ] ); }
		};
		fns.listValueCount = {
			string: false,
			valid: function( l, e ) { return true; },
			args: function( l, e ) { return [ l, e ]; },
			refArgs: function( l, e ) { return [ l, e ]; },
			multi: function( a ) { return listValueCount( a[ 1 ], "a", variables.delimiter, a[ 2 ], true ); },
			ref: function( a ) { return listValueCount( a[ 1 ], "a", variables.sentinel, a[ 2 ] ); }
		};
		fns.listValueCountNoCase = {
			string: false,
			valid: function( l, e ) { return true; },
			args: function( l, e ) { return [ l, e ]; },
			refArgs: function( l, e ) { return [ l, e ]; },
			multi: function( a ) { return listValueCountNoCase( a[ 1 ], "a", variables.delimiter, a[ 2 ], true ); },
			ref: function( a ) { return listValueCountNoCase( a[ 1 ], "a", variables.sentinel, a[ 2 ] ); }
		};
		fns.listItemTrim = {
			string: true,
			valid: function( l, e ) { return true; },
			args: function( l, e ) { return [ l, e ]; },
			refArgs: function( l, e ) { return [ l, e ]; },
			multi: function( a ) { return listItemTrim( a[ 1 ], variables.delimiter, a[ 2 ], true ); },
			ref: function( a ) { return listItemTrim( a[ 1 ], variables.sentinel, a[ 2 ] ); }
		};
		fns.listSort = {
			string: true,
			valid: function( l, e ) { return true; },
			args: function( l, e ) { return [ l, e ]; },
			refArgs: function( l, e ) { return [ l, e ]; },
			multi: function( a ) { return listSort( a[ 1 ], "text", "asc", variables.delimiter, a[ 2 ], true ); },
			ref: function( a ) { return listSort( a[ 1 ], "text", "asc", variables.sentinel, a[ 2 ] ); }
		};
		fns.listQualify = {
			string: true,
			valid: function( l, e ) { return true; },
			args: function( l, e ) { return [ l, e ]; },
			refArgs: function( l, e ) { return [ l, e ]; },
			multi: function( a ) { return listQualify( a[ 1 ], "'", variables.delimiter, "all", a[ 2 ], true ); },
			ref: function( a ) { return listQualify( a[ 1 ], "'", variables.sentinel, "all", a[ 2 ] ); }
		};
		fns.listPrepend = {
			string: true,
			// the single character listPrepend() returns "X," for a list containing only delimiters and includeEmptyFields=false, with a multi character delimiter this is "X" (same as listAppend()), see the explicit test
			valid: function( l, e ) { return e || !len( l ) || listLen( l, variables.sentinel, false ) > 0; },
			args: function( l, e ) { return [ l, e ]; },
			refArgs: function( l, e ) { return [ l, e ]; },
			multi: function( a ) { return listPrepend( a[ 1 ], "X", variables.delimiter, a[ 2 ], true ); },
			ref: function( a ) { return listPrepend( a[ 1 ], "X", variables.sentinel, a[ 2 ] ); }
		};
		fns.listGetDuplicates = {
			string: true,
			valid: function( l, e ) { return true; },
			args: function( l, e ) { return [ l, e ]; },
			refArgs: function( l, e ) { return [ l, e ]; },
			multi: function( a ) { return listGetDuplicates( a[ 1 ], variables.delimiter, true, a[ 2 ], true ); },
			ref: function( a ) { return listGetDuplicates( a[ 1 ], variables.sentinel, true, a[ 2 ] ); }
		};
		fns.listRemoveDuplicates = {
			string: true,
			valid: function( l, e ) { return true; },
			args: function( l, e ) { return [ l, e ]; },
			refArgs: function( l, e ) { return [ l, e ]; },
			multi: function( a ) { return listRemoveDuplicates( a[ 1 ], variables.delimiter, true, true ); },
			ref: function( a ) { return listRemoveDuplicates( a[ 1 ], variables.sentinel, true ); }
		};
		return fns;
	}

}
