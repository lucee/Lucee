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
				} ).toThrow( "", "Component is not enabled" );
			});

			it( title="should throw when swagger / openapi disabled", body=function( currentSpec ){
				expect( function() {
					var result = internalRequest( 
						template: "/test/openapi/artifacts/openapi_base.cfc", 
						url: "swagger"
					);
				} ).toThrow( "", "Component is not enabled" );
				expect( function() {
					var result = internalRequest( 
						template: "/test/openapi/artifacts/openapi_base.cfc", 
						url: "openapi"
					);
				} ).toThrow( "", "Component is not enabled" );
			});

			it( title="fetch swagger", body=function( currentSpec ){
				var result = internalRequest( 
					template: "/test/openapi/artifacts/openapi_swagger.cfc", 
					url: "swagger"
				);
				expect( result.filecontent ) .toInclude( "swagger-ui-bundle.js");
			});
		});

		describe("check openApi against getMetadata", function() {
			it( title="compare openapi service metadata against cfc metadata", body=function( currentSpec ){
				var result = internalRequest( 
					template: "/test/openapi/artifacts/openapi_only.cfc", 
					url: "openapi"
				);
				debug ( result.filecontent );
				expect( result.filecontent ).toBeJson( result.filecontent );
				var openApiMetadata = deserializeJSON( result.filecontent );

				var cfc = new artifacts.openapi_only();
				var cfcMetadata = getMetaData(cfc);
				
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
				
				var openApiMetadata = deserializeJSON(result.filecontent);
				var cfc = new artifacts.openapi_only();
				var cfcMetadata = getMetaData(cfc);

				var openApiFfunctions = {};
				for (var f in openApiMetadata.extends.functions ){
					openApiFfunctions[ f.name ] = true;
				}
				for ( var f in cfcMetadata.extends.functions ){
					expect ( openApiFfunctions ).toHaveKey( f.name );
				}
			});

			it( title="LDEV-5783 compare createObject(openapi) metadata against cfc metadata", body=function( currentSpec ){
				
				var openApiCFC= createObject("openapi", "/test/openapi/artifacts/openapi_only.cfc?openapi");
				var openApiMetadata = getMetadata( openApiCFC );

				var cfc = new artifacts.openapi_only();
				var cfcMetadata = getMetaData(cfc);
				
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