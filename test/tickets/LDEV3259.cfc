component extends="org.lucee.cfml.test.LuceeTestCase" labels="functions,tags,extensions" {

	function run( testResults, testBox ) {
		describe( "LDEV-3259 getTagData() and getFunctionData() should include extension source", function() {

			it( title="getFunctionData() has no source for a core function", body=function( currentSpec ) {
				var data = getFunctionData( "len" );
				expect( data ).toBeTypeOf( "struct" );
				expect( structKeyExists( data, "name" ) ).toBeTrue();
				expect( data.name ).toBe( "len" );
				expect( structKeyExists( data, "source" ) ).toBeFalse();
			});

			it( title="getTagData() has no source for a core tag", body=function( currentSpec ) {
				var data = getTagData( "cf", "output" );
				expect( data ).toBeTypeOf( "struct" );
				expect( structKeyExists( data, "name" ) ).toBeTrue();
				expect( structKeyExists( data, "source" ) ).toBeFalse();
			});

			it( title="getFunctionData() still returns the existing keys", body=function( currentSpec ) {
				var data = getFunctionData( "arraylen" );
				expect( structKeyExists( data, "name" ) ).toBeTrue();
				expect( structKeyExists( data, "status" ) ).toBeTrue();
				expect( structKeyExists( data, "arguments" ) ).toBeTrue();
				expect( structKeyExists( data, "returnType" ) ).toBeTrue();
			});

			it( title="getTagData() still returns the existing keys", body=function( currentSpec ) {
				var data = getTagData( "cf", "query" );
				expect( structKeyExists( data, "name" ) ).toBeTrue();
				expect( structKeyExists( data, "status" ) ).toBeTrue();
				expect( structKeyExists( data, "attributes" ) ).toBeTrue();
				expect( structKeyExists( data, "nameSpace" ) ).toBeTrue();
			});

			it( title="getFunctionList() without an extension still returns the functions", body=function( currentSpec ) {
				var list = getFunctionList();
				expect( list ).toBeTypeOf( "struct" );
				expect( structCount( list ) ).toBeGT( 0 );
			});

			it( title="getTagList() without an extension still returns the tags", body=function( currentSpec ) {
				var list = getTagList();
				expect( list ).toBeTypeOf( "struct" );
				expect( structCount( list ) ).toBeGT( 0 );
			});

			it( title="getFunctionList() with an empty extension returns all functions", body=function( currentSpec ) {
				var allFunctions = getFunctionList();
				var filteredFunctions = getFunctionList( "" );
				expect( structCount( filteredFunctions ) ).toBe( structCount( allFunctions ) );
			});

			it( title="getTagList() with an empty extension returns all tags", body=function( currentSpec ) {
				var allTags = getTagList();
				var filteredTags = getTagList( "" );
				expect( structCount( filteredTags ) ).toBe( structCount( allTags ) );
			});

			it( title="getFunctionList() with an unknown extension guid returns no functions", body=function( currentSpec ) {
				var filtered = getFunctionList( "00000000-0000-0000-0000-000000000000" );
				expect( filtered ).toBeTypeOf( "struct" );
				expect( structCount( filtered ) ).toBe( 0 );
			});

			it( title="getTagList() with an unknown extension guid returns no tags", body=function( currentSpec ) {
				var filtered = getTagList( "00000000-0000-0000-0000-000000000000" );
				expect( filtered ).toBeTypeOf( "struct" );
				expect( structCount( filtered ) ).toBe( 0 );
			});

			it( title="getFunctionList() and getTagList() accept extension as a named argument", body=function( currentSpec ) {
				var filteredFuncs = getFunctionList( extension="00000000-0000-0000-0000-000000000000" );
				expect( filteredFuncs ).toBeTypeOf( "struct" );
				expect( structCount( filteredFuncs ) ).toBe( 0 );

				var filteredTags = getTagList( extension="00000000-0000-0000-0000-000000000000" );
				expect( filteredTags ).toBeTypeOf( "struct" );
				expect( structCount( filteredTags ) ).toBe( 0 );
			});

			it( title="getFunctionData() and getTagData() return the extension guid as source and the lists filter by it", body=function( currentSpec ) {
				var imageGuid = "B737ABC4-D43F-4D91-8E8E973E37C40D1B";
				var extFuncs = getFunctionList( imageGuid );
				expect( structKeyExists( extFuncs, "imagenew" ) ).toBeTrue();
				expect( structKeyExists( extFuncs, "len" ) ).toBeFalse();
				expect( getFunctionData( "imagenew" ).source ).toBe( imageGuid );

				var extTags = getTagList( imageGuid );
				expect( structKeyExists( extTags, "cf" ) ).toBeTrue();
				expect( structKeyExists( extTags.cf, "image" ) ).toBeTrue();
				expect( structKeyExists( extTags.cf, "output" ) ).toBeFalse();
				expect( getTagData( "cf", "image" ).source ).toBe( imageGuid );
			});

			it( title="extension filter is case insensitive and does not match a partial guid", body=function( currentSpec ) {
				var imageGuid = "B737ABC4-D43F-4D91-8E8E973E37C40D1B";
				expect( structKeyExists( getFunctionList( lCase( imageGuid ) ), "imagenew" ) ).toBeTrue();
				expect( structCount( getFunctionList( left( imageGuid, 8 ) ) ) ).toBe( 0 );
				expect( structCount( getTagList( left( imageGuid, 8 ) ) ) ).toBe( 0 );
			});

		});
	}
}
