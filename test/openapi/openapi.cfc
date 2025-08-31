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

		xdescribe("remote components should call onError method", function() {
			it( title="generate error", body=function( currentSpec ){
				var result = internalRequest(
					template: "/test/openapi/artifacts/openapi_only.cfc",
					url: "method=getDateFormatted&returnFormat=json&date=lucee",
					throwOnError: false
				);
				expect( result.filecontent ).toBeJson( );
				var error = deserializeJSON( result.filecontent );
				expect( error ).toHaveKey( "error" );
				expect( error.error).toBeTrue();
				expect( error.message ).toInclude( "Invalid call of the function [getDateFormatted], first Argument [date] is of invalid type" );
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
					cfcFunctions[ f.name ] = true;
				}
				for (var path in openApiMetadata.paths){
					expect ( cfcFunctions ).toHaveKey( listLast( path,"=" ) );
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

	}

}