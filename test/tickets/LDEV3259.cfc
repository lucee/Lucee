component extends="org.lucee.cfml.test.LuceeTestCase" labels="extensions" {

    function run( testResults, testBox ) {
        describe( "LDEV-3259: getTagData() and getFunctionData() extension metadata", function() {

            it( title="getFunctionData() for a core function has no extension key", body=function( currentSpec ) {
                var data = getFunctionData( "len" );

                expect( data ).toBeTypeOf( "struct" );
                expect( structKeyExists( data, "name" ) ).toBeTrue();
                expect( data.name ).toBe( "len" );
                expect( structKeyExists( data, "extension" ) ).toBeFalse();
            });

            it( title="getTagData() for a core tag has no extension key", body=function( currentSpec ) {
                var data = getTagData( "cf", "output" );

                expect( data ).toBeTypeOf( "struct" );
                expect( structKeyExists( data, "name" ) ).toBeTrue();
                expect( structKeyExists( data, "extension" ) ).toBeFalse();
            });

            it( title="getFunctionData() returns expected metadata keys", body=function( currentSpec ) {
                var data = getFunctionData( "arraylen" );

                expect( structKeyExists( data, "name" ) ).toBeTrue();
                expect( structKeyExists( data, "status" ) ).toBeTrue();
                expect( structKeyExists( data, "arguments" ) ).toBeTrue();
                expect( structKeyExists( data, "returnType" ) ).toBeTrue();
            });

            it( title="getTagData() returns expected metadata keys", body=function( currentSpec ) {
                var data = getTagData( "cf", "query" );

                expect( structKeyExists( data, "name" ) ).toBeTrue();
                expect( structKeyExists( data, "status" ) ).toBeTrue();
                expect( structKeyExists( data, "attributes" ) ).toBeTrue();
                expect( structKeyExists( data, "nameSpace" ) ).toBeTrue();
            });

            it( title="getFunctionList() returns a non-empty struct", body=function( currentSpec ) {
                var functions = getFunctionList();

                expect( functions ).toBeTypeOf( "struct" );
                expect( structCount( functions ) ).toBeGT( 0 );
            });

            it( title="getTagList() returns a non-empty struct", body=function( currentSpec ) {
                var tags = getTagList();

                expect( tags ).toBeTypeOf( "struct" );
                expect( structCount( tags ) ).toBeGT( 0 );
            });

            it( title="getFunctionList() with an empty extension filter returns all functions", body=function( currentSpec ) {
                var allFunctions = getFunctionList();
                var filteredFunctions = getFunctionList( "" );

                expect( filteredFunctions ).toBeTypeOf( "struct" );
                expect( structCount( filteredFunctions ) ).toBe( structCount( allFunctions ) );
            });

            it( title="getTagList() with an empty extension filter returns all tags", body=function( currentSpec ) {
                var allTags = getTagList();
                var filteredTags = getTagList( "" );

                expect( filteredTags ).toBeTypeOf( "struct" );
                expect( structCount( filteredTags ) ).toBe( structCount( allTags ) );
            });

            it( title="getFunctionList() with an unknown extension GUID returns an empty struct", body=function( currentSpec ) {
                var filtered = getFunctionList( "00000000-0000-0000-0000-000000000000" );

                expect( filtered ).toBeTypeOf( "struct" );
                expect( structCount( filtered ) ).toBe( 0 );
            });

            it( title="getTagList() with an unknown extension GUID returns an empty struct", body=function( currentSpec ) {
                var filtered = getTagList( "00000000-0000-0000-0000-000000000000" );

                expect( filtered ).toBeTypeOf( "struct" );
                expect( structCount( filtered ) ).toBe( 0 );
            });

            it( title="getFunctionList() and getTagList() support the named extension argument", body=function( currentSpec ) {
                var unknownGuid = "00000000-0000-0000-0000-000000000000";

                var filteredFunctions = getFunctionList( extension=unknownGuid );
                expect( filteredFunctions ).toBeTypeOf( "struct" );
                expect( structCount( filteredFunctions ) ).toBe( 0 );

                var filteredTags = getTagList( extension=unknownGuid );
                expect( filteredTags ).toBeTypeOf( "struct" );
                expect( structCount( filteredTags ) ).toBe( 0 );
            });

            var compressGuid = "8D7FB0DF-08BB-1589-FE3975678F07DB17";
            var allFunctions = getFunctionList();
            var allTags = getTagList();

            it( title="bundled compress extension functions include exact extension GUID", body=function( currentSpec ) {
                var extensionFunctions = getFunctionList( compressGuid );

                expect( structKeyExists( extensionFunctions, "compress" ) ).toBeTrue();

                var functionData = getFunctionData( "compress" );

                expect( structKeyExists( functionData, "extension" ) ).toBeTrue();
                expect( functionData.extension ).toBe( compressGuid );
            });

            it( title="bundled compress extension tags include exact extension GUID", body=function( currentSpec ) {
                var extensionTags = getTagList( compressGuid );

                expect( structKeyExists( extensionTags, "cf" ) ).toBeTrue();
                expect( structKeyExists( extensionTags.cf, "zip" ) ).toBeTrue();

                var tagData = getTagData( "cf", "zip" );

                expect( structKeyExists( tagData, "extension" ) ).toBeTrue();
                expect( tagData.extension ).toBe( compressGuid );
            });

            it( title="extension filter matches case-insensitively and does not do substring or path matching", body=function( currentSpec ) {
                var lowerCompress = lCase( compressGuid );
                var filtered = getFunctionList( lowerCompress );
                expect( structKeyExists( filtered, "compress" ) ).toBeTrue();

                var partial = left( compressGuid, 8 );
                expect( structCount( getFunctionList( partial ) ) ).toBe( 0 );
                expect( structCount( getTagList( partial ) ) ).toBe( 0 );

                expect( structCount( getFunctionList( "lib" ) ) ).toBe( 0 );
                expect( structCount( getTagList( "lib" ) ) ).toBe( 0 );
            });

            var imageGuid = "B737ABC4-D43F-4D91-8E8E973E37C40D1B";

            it( title = "Image extension functions include extension metadata when the extension is installed", skip = !structKeyExists( allFunctions, "imagenew" ), body = function( currentSpec ) {
                var extensionFunctions = getFunctionList( imageGuid );

                expect( structKeyExists( extensionFunctions, "imagenew" ) ).toBeTrue();

                var functionData = getFunctionData( "imagenew" );

                expect( structKeyExists( functionData, "extension" ) ).toBeTrue();
                expect( functionData.extension ).toBe( imageGuid );
            });

            it( title = "Image extension tags include extension metadata when the extension is installed", skip = !(structKeyExists( allTags, "cf" ) && structKeyExists( allTags[ "cf" ], "image" )), body = function( currentSpec ) {
                var extensionTags = getTagList( imageGuid );

                expect( structKeyExists( extensionTags, "cf" ) ).toBeTrue();
                expect( structKeyExists( extensionTags[ "cf" ], "image" ) ).toBeTrue();

                var tagData = getTagData( "cf", "image" );

                expect( structKeyExists( tagData, "extension" ) ).toBeTrue();
                expect( tagData.extension ).toBe( imageGuid );
            });

        });
    }
}
