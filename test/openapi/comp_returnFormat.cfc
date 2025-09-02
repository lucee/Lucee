component extends="org.lucee.cfml.test.LuceeTestCase" labels="openapi" {
	function run( testResults, testBox ) {

		var returnFormats = [ "xml", "json", "plain", "wddx", "default" ];

		describe("LDEV-5789 remote component functions should inherit component returnFormat", function() {
			loop array="#returnFormats#" item="local.format" {
				it( title="return format #format#",
					data = { format: format },
					body = function( data ){
					var template = "/test/openapi/artifacts/comp_returnFormat";
					if ( len( data.format ) ) {
						template &= "_" & data.format & ".cfc";
					}
					
					var result = internalRequest(
						template: template,
						url: "method=testReturn&value=lucee"
					);
					
					expect( result.headers ).toHaveKey( "Return-Format" );
					if ( data.format eq "default" )
					expect( result.headers[ "Return-Format" ] )
						.toBe( data.format eq "default" ? "wddx" :data.format );

					// TODO LDEV-2995 content type has a trailing ;charset=utf-8, so using toInclude rather that toBe
					switch ( data.format ){
						case "default":
						case "xml":
						case "wddx":
							expect ( isXml( result.filecontent ) ).toBeTrue();
							expect( result.headers[ "Content-type" ] ).toInclude( "text/xml" );
							break;
						case "json":
							expect ( isJson( result.filecontent ) ).toBeTrue();
							expect( result.headers[ "Content-type" ] ).toInclude( "application/json" );
							break;
						case "plain":
							expect ( result.filecontent ).toBe( "lucee" );
							expect( result.headers[ "Content-type" ] ).toInclude( "text/plain" );
							break;
						default:
							throw "unknown return format: [" & data.format & "]";
					}

				});
			};
		});

	}

}