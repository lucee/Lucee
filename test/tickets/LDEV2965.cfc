component extends="org.lucee.cfml.test.LuceeTestCase" {

	function beforeAll() {
		variables.uri = createURI( "LDEV2965" );
		variables.remoteFns = [ "getFileName", "getLine", "getBuffered", "ping" ];
		variables.hiddenFns = [ "onRequestStart", "sharedFn", "packageFn", "localFn" ];
	}

	function run( testResults, testBox ) {
		describe( "LDEV-2965", function() {

			it( title="remote function with returnformat plain returns exactly the returned string", body=function() {
				var res = _InternalRequest(
					template: "#variables.uri#/Service.cfc",
					url: { method: "getFileName" }
				);
				expect( res.fileContent ).toBe( "report.txt" );
				expect( res.fileContent.len() ).toBe( len( "report.txt" ) );

				var resUrl = _InternalRequest(
					template: "#variables.uri#/Service.cfc",
					url: { method: "getFileName", returnformat: "plain" }
				);
				expect( resUrl.fileContent ).toBe( "report.txt" );

				var resLine = _InternalRequest(
					template: "#variables.uri#/Service.cfc",
					url: { method: "getLine" }
				);
				expect( resLine.fileContent ).toBe( "line" & chr( 10 ) );
			});

			it( title="remote function with returnformat plain keeps buffered output", body=function() {
				var res = _InternalRequest(
					template: "#variables.uri#/Service.cfc",
					url: { method: "getBuffered" }
				);
				expect( res.fileContent ).toBe( "BUFFERED-RETURN" );
			});

			it( title="non remote functions can not be called remotely", body=function() {
				loop array=variables.hiddenFns item="local.fn" {
					expect( function() {
						_InternalRequest(
							template: "#variables.uri#/Service.cfc",
							url: { method: fn }
						);
					}).toThrow( regex="no remote function" );
				}
			});

			loop list="json,json=,cfml,wddx,xml" item="local.qs" {
				it( title="remote proxy metadata [?#qs#] only lists remote functions", data={ qs: qs }, body=function( data ) {
					var fns = listedFunctions( "Service.cfc", data.qs );
					loop array=variables.remoteFns item="local.fn" {
						expect( fns ).toInclude( lCase( fn ) );
					}
					loop array=variables.hiddenFns item="local.fn" {
						expect( fns ).notToInclude( lCase( fn ) );
					}
					expect( fns.len() ).toBe( variables.remoteFns.len() );

					var childFns = listedFunctions( "ChildService.cfc", data.qs );
					loop array=variables.remoteFns.merge( [ "childPing" ] ) item="local.fn" {
						expect( childFns ).toInclude( lCase( fn ) );
					}
					loop array=variables.hiddenFns.merge( [ "childShared", "childLocal" ] ) item="local.fn" {
						expect( childFns ).notToInclude( lCase( fn ) );
					}
				});
			}

		});
	}

	private array function listedFunctions( required string cfc, required string qs ) {
		var content = _InternalRequest(
			template: "#variables.uri#/#arguments.cfc#",
			url: arguments.qs
		).fileContent;
		var format = listFirst( arguments.qs, "=" );
		var names = [];
		if ( format == "json" ) {
			names = structKeyArray( deserializeJSON( content ).functions );
		}
		else if ( format == "cfml" ) {
			names = structKeyArray( evaluate( content ).functions );
		}
		else if ( format == "wddx" ) {
			wddx action="wddx2cfml" input=content output="local.meta";
			names = structKeyArray( meta.functions );
		}
		else {
			var nodes = xmlSearch( xmlParse( content ), "/STRUCT/ENTRY[@NAME='functions']/STRUCT/ENTRY" );
			loop array=nodes item="local.node" {
				names.append( node.xmlAttributes.NAME );
			}
		}
		return names.map( function( n ) { return lCase( n ); } );
	}

	private string function createURI( string calledName ) {
		var baseURI = "/test/#listLast( getDirectoryFromPath( getCurrentTemplatePath() ), "\/" )#/";
		return baseURI & "" & calledName;
	}

}
