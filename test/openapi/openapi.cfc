component extends="org.lucee.cfml.test.LuceeTestCase" labels="openapi" {
	function run( testResults, testBox ) {
		describe("Testcase for openapi", function() {
			it( title="fetch metadata as openapi", body=function( currentSpec ){
				var result = internalRequest(
					template: "/test/openapi/artifacts/openapi_only.cfc",
					url: "openapi"
				);
				expect( result.filecontent ).toBeJson( result.filecontent );
			});

			it( title="should throw when swagger disabled", body=function( currentSpec ){
				expect( function() {
					var result = internalRequest(
						template: "/test/openapi/artifacts/openapi_only.cfc",
						url: "swagger"
					);
					if (result.status_code neq 200) {
						throw result.filecontent;
					}
				} ).toThrow( "", "Component is not enabled" );
			});

			it( title="should throw when swagger / openapi disabled", body=function( currentSpec ){
				expect( function() {
					var result = internalRequest(
						template: "/test/openapi/artifacts/openapi_base.cfc",
						url: "swagger"
					);
					if (result.status_code neq 200) {
						throw result.filecontent;
					}
				} ).toThrow( "", "Component is not enabled" );
				expect( function() {
					var result = internalRequest(
						template: "/test/openapi/artifacts/openapi_base.cfc",
						url: "openapi"
					);
					if (result.status_code neq 200) {
						throw result.filecontent;
					}
				} ).toThrow( "", "Component is not enabled for OpenAPI" );
			});

			it( title="fetch swagger ui", body=function( currentSpec ){
				var result = internalRequest(
					template: "/test/openapi/artifacts/openapi_swagger.cfc",
					url: "swagger"
				);
				expect( result.filecontent ) .toInclude( "swagger-ui-bundle.js", result.filecontent);
			});

			it( title="fetch swagger ui with custom version", body=function( currentSpec ){
				var result = internalRequest(
					template: "/test/openapi/artifacts/openapi_swagger_custom_version.cfc",
					url: "swagger"
				);
				expect( result.filecontent ) .toInclude( "swagger-ui-bundle.js", result.filecontent);
				expect( result.filecontent ) .toInclude( "4.0.5", result.filecontent);
			});

		});

		describe("remote components should call onError method", function() {
			it( title="firstly check works as expected with valid date", body=function( currentSpec ){
				var testDate = "1-jan-2025";
				var result = internalRequest(
					template: "/test/openapi/artifacts/openapi_only.cfc",
					url: "method=getDateFormatted&returnFormat=json&date=#testDate#"
				);
				expect( result.filecontent ).toBeJson( );
				var obj = deserializeJSON( result.filecontent );
				expect( obj ).toHaveKey( "result" );
				expect( obj.result ).toBe( dateFormat( testDate, "yyyy-mm-dd" ) );
			});

			xit( title="call method with invalid date, causing error ", body=function( currentSpec ){
				var result = internalRequest(
					template: "/test/openapi/artifacts/openapi_only.cfc",
					url: "method=getDateFormatted&returnFormat=json&date=lucee",
					throwOnError: false
				);
				expect( result.filecontent ).toBeJson( );
				var obj = deserializeJSON( result.filecontent );
				expect( obj ).toHaveKey( "error" );
				expect( obj.error ).toBeTrue();
				expect( obj.message ).toInclude( "Invalid call of the function [getDateFormatted], first Argument [date] is of invalid type" );
			});
		});

		describe("check openApi against getMetadata", function() {
			it( title="compare openapi service metadata against cfc metadata", body=function( currentSpec ){
				var result = internalRequest(
					template: "/test/openapi/artifacts/openapi_only.cfc",
					url: "openapi"
				);
				expect( result.filecontent ).toBeJson( result.filecontent );
				var openApiMetadata = deserializeJSON( result.filecontent );

				var cfc = new artifacts.openapi_only();
				var cfcMetadata = getMetaData( cfc );

				var cfcFunctions = {};
				for (var f in cfcMetadata.extends.functions){
					cfcFunctions[ f.name ] = {
						access: f.access,
						name: f.name
					};
				}

				for (var path in openApiMetadata.paths){
					var method = listLast( path, "=" );
					expect ( cfcFunctions ).toHaveKey( method );
					// check method has the same case
					expect ( method ).toBeWithCase( cfcFunctions[ method ].name );
					
					// functions the openapi spec must have access = remote
					if ( cfcFunctions[ method ].access != "remote" ) {
						expect( false ).toBeTrue( "non remote function [#method#] should not be in openapi spec" );
					}
				}
			});

			// this is just comparing the same function from a remote method
			it( title="compare metadata against cfc metadata", body=function( currentSpec ){
				var result = internalRequest(
					template: "/test/openapi/artifacts/openapi_only.cfc",
					url: "method=getMetadata&returnFormat=json"
				);
				expect( result.filecontent ).toBeJson( result.filecontent );
				var openApiMetadata = deserializeJSON( result.filecontent );
				var cfc = new artifacts.openapi_only();
				var cfcMetadata = getMetaData( cfc );

				var openApiFfunctions = {};
				for (var f in openApiMetadata.extends.functions ){
					openApiFfunctions[ f.name ] = true;
				}
				for ( var f in cfcMetadata.extends.functions ){
					expect ( openApiFfunctions ).toHaveKey( f.name );
				}
			});

			xit( title="LDEV-5783 compare createObject(openapi) metadata against cfc metadata", body=function( currentSpec ){

				var openApiCFC = createObject("openapi", "/test/openapi/artifacts/openapi_only.cfc?openapi");
				var openApiMetadata = getMetadata( openApiCFC );

				var cfc = new artifacts.openapi_only();
				var cfcMetadata = getMetaData( cfc );

				var openApiFfunctions = {};
				for ( var f in openApiMetadata.functions ){ // extends should be abstracted away
					openApiFfunctions[ f.name ] = true;
				}
				for ( var f in cfcMetadata.extends.functions ){
					expect ( openApiFfunctions ).toHaveKey( f.name );
				}
			});

		});

		describe("check array syntax for args, i.e. name[]", function() {
			it( title="compare metadata against cfc metadata", body=function( currentSpec ){
				var arrSuffix = urlEncode("[]");
				var result = internalRequest(
					template: "/test/openapi/artifacts/openapi_only.cfc",
					url: "method=getArrayAsString&returnFormat=json&arr#arrSuffix#=1&arr#arrSuffix#=2"
				);
				expect( result.filecontent ).toBeJson( result.filecontent );
				var obj = deserializeJSON( result.filecontent );
				expect( obj ).toHaveKey( "result" );
				expect( obj.result ).toBe("1$2");;
			});
		});

	}

}