component extends="org.lucee.cfml.test.LuceeTestCase" labels="functions,tags,extensions" {

	function run( testResults, testBox ) {
		describe( "LDEV-3259: getTagData() and getFunctionData() should include extension source", function() {

			it( title="getFunctionData() for a core function has no source key (built-in functions have no file source)", body=function( currentSpec ) {
				var data = getFunctionData( "len" );
				expect( data ).toBeTypeOf( "struct" );
				expect( structKeyExists( data, "name" ) ).toBeTrue();
				expect( data.name ).toBe( "len" );
			});

			it( title="getTagData() for a core tag has no source key (built-in tags have no file source)", body=function( currentSpec ) {
				var data = getTagData( "cf", "output" );
				expect( data ).toBeTypeOf( "struct" );
				expect( structKeyExists( data, "name" ) ).toBeTrue();
			});

			it( title="getFunctionData() returns correct struct keys", body=function( currentSpec ) {
				var data = getFunctionData( "arraylen" );
				expect( structKeyExists( data, "name" ) ).toBeTrue();
				expect( structKeyExists( data, "status" ) ).toBeTrue();
				expect( structKeyExists( data, "arguments" ) ).toBeTrue();
				expect( structKeyExists( data, "returnType" ) ).toBeTrue();
			});

			it( title="getTagData() returns correct struct keys", body=function( currentSpec ) {
				var data = getTagData( "cf", "query" );
				expect( structKeyExists( data, "name" ) ).toBeTrue();
				expect( structKeyExists( data, "status" ) ).toBeTrue();
				expect( structKeyExists( data, "attributes" ) ).toBeTrue();
				expect( structKeyExists( data, "nameSpace" ) ).toBeTrue();
			});

			it( title="getFunctionList() returns a non-empty struct", body=function( currentSpec ) {
				var list = getFunctionList();
				expect( list ).toBeTypeOf( "struct" );
				expect( structCount( list ) ).toBeGT( 0 );
			});

			it( title="getTagList() returns a non-empty struct", body=function( currentSpec ) {
				var list = getTagList();
				expect( list ).toBeTypeOf( "struct" );
				expect( structCount( list ) ).toBeGT( 0 );
			});

			it( title="getFunctionList() with empty extension filter returns all functions", body=function( currentSpec ) {
				var allFunctions = getFunctionList();
				var filteredFunctions = getFunctionList( "" );
				expect( structCount( filteredFunctions ) ).toBe( structCount( allFunctions ) );
			});

			it( title="getTagList() with empty extension filter returns all tags", body=function( currentSpec ) {
				var allTags = getTagList();
				var filteredTags = getTagList( "" );
				expect( structCount( filteredTags ) ).toBe( structCount( allTags ) );
			});

			it( title="getFunctionList() with a non-existent extension GUID returns empty struct", body=function( currentSpec ) {
				var filtered = getFunctionList( "00000000-0000-0000-0000-000000000000" );
				expect( filtered ).toBeTypeOf( "struct" );
				expect( structCount( filtered ) ).toBe( 0 );
			});

			it( title="getTagList() with a non-existent extension GUID returns empty struct", body=function( currentSpec ) {
				var filtered = getTagList( "00000000-0000-0000-0000-000000000000" );
				expect( filtered ).toBeTypeOf( "struct" );
				expect( structCount( filtered ) ).toBe( 0 );
			});

			it( title="getFunctionList(extension=...) and getTagList(extension=...) support named argument", body=function( currentSpec ) {
				var filteredFuncs = getFunctionList( extension="00000000-0000-0000-0000-000000000000" );
				expect( filteredFuncs ).toBeTypeOf( "struct" );
				expect( structCount( filteredFuncs ) ).toBe( 0 );

				var filteredTags = getTagList( extension="00000000-0000-0000-0000-000000000000" );
				expect( filteredTags ).toBeTypeOf( "struct" );
				expect( structCount( filteredTags ) ).toBe( 0 );
			});

			it( title="extension functions and tags include extension source when installed", body=function( currentSpec ) {
				var imageGuid = "B737ABC4-D43F-4D91-8E8E973E37C40D1B";
				var allFuncs = getFunctionList();
				if ( structKeyExists( allFuncs, "imagenew" ) ) {
					var extFuncs = getFunctionList( imageGuid );
					expect( structCount( extFuncs ) ).toBeGT( 0 );
					expect( structKeyExists( extFuncs, "imagenew" ) ).toBeTrue();

					var funcData = getFunctionData( "imagenew" );
					expect( structKeyExists( funcData, "source" ) ).toBeTrue();
					expect( len( funcData.source ) ).toBeGT( 0 );
					expect( funcData.source ).toInclude( imageGuid );
				}

				var allTags = getTagList();
				if ( structKeyExists( allTags, "cf:" ) && structKeyExists( allTags[ "cf:" ], "image" ) ) {
					var extTags = getTagList( imageGuid );
					expect( structCount( extTags ) ).toBeGT( 0 );
					expect( structKeyExists( extTags, "cf:" ) ).toBeTrue();
					expect( structKeyExists( extTags[ "cf:" ], "image" ) ).toBeTrue();

					var tagData = getTagData( "cf", "image" );
					expect( structKeyExists( tagData, "source" ) ).toBeTrue();
					expect( len( tagData.source ) ).toBeGT( 0 );
					expect( tagData.source ).toInclude( imageGuid );
				}
			});

		});
	}
}
