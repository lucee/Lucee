component extends="org.lucee.cfml.test.LuceeTestCase" labels="dump,internalRequest" {

	function beforeAll() {
		variables.uri = createURI( "LDEV1639" );
	}

	function run( testResults, testBox ) {
		describe( "LDEV-1639: dump() and writeDump() use the defaults of this.tag.dump", function() {

			it( title="the tag uses this.tag.dump.format (reference)", body=function( currentSpec ) {
				var result = req( "tag" );
				expect( result ).toInclude( "format: text" );
				expect( result ).toInclude( "Susi" );
			});

			it( title="dump() uses this.tag.dump.format", body=function( currentSpec ) {
				var result = req( "dump" );
				expect( result ).toInclude( "format: text" );
				expect( result ).toInclude( "Susi" );
			});

			it( title="dump() with a positional argument uses this.tag.dump.format", body=function( currentSpec ) {
				var result = req( "dumpPositional" );
				expect( result ).toInclude( "format: text" );
				expect( result ).toInclude( "Susi" );
			});

			it( title="writeDump() uses this.tag.dump.format", body=function( currentSpec ) {
				var result = req( "writeDump" );
				expect( result ).toInclude( "format: text" );
				expect( result ).toInclude( "Susi" );
			});

			it( title="writeDump() with a positional argument uses this.tag.dump.format", body=function( currentSpec ) {
				var result = req( "writeDumpPositional" );
				expect( result ).toInclude( "format: text" );
				expect( result ).toInclude( "Susi" );
			});

			it( title="dump() and writeDump() give the same output as the tag", body=function( currentSpec ) {
				var tagResult = req( "tag" );
				expect( normalize( req( "dump" ) ) ).toBe( normalize( tagResult ) );
				expect( normalize( req( "writeDump" ) ) ).toBe( normalize( tagResult ) );
			});

			it( title="an argument of dump() overrides this.tag.dump.format", body=function( currentSpec ) {
				expect( req( "dumpHtml" ) ).toInclude( "format: html" );
			});

			it( title="an argument of writeDump() overrides this.tag.dump.format", body=function( currentSpec ) {
				expect( req( "writeDumpHtml" ) ).toInclude( "format: html" );
			});

			it( title="dump() and writeDump() use this.tag.dump.output", body=function( currentSpec ) {
				// output console writes to the console, nothing in the response
				expect( trim( req( "tag", { output: "console" } ) ) ).toBe( "" );
				expect( trim( req( "dump", { output: "console" } ) ) ).toBe( "" );
				expect( trim( req( "writeDump", { output: "console" } ) ) ).toBe( "" );
			});

			it( title="the output argument of dump() and writeDump() overrides this.tag.dump.output", body=function( currentSpec ) {
				expect( req( "dumpBrowser", { output: "console" } ) ).toInclude( "Susi" );
				expect( req( "writeDumpBrowser", { output: "console" } ) ).toInclude( "Susi" );
			});

			it( title="without this.tag.dump the default format is used (unchanged)", body=function( currentSpec ) {
				expect( req( "tag", {}, "plain/" ) ).toInclude( "format: html" );
				expect( req( "dump", {}, "plain/" ) ).toInclude( "format: html" );
				expect( req( "writeDump", {}, "plain/" ) ).toInclude( "format: html" );
			});

		});
	}

	// removes the parts that change from request to request (timestamp, element id)
	private string function normalize( required string html ) {
		return reReplace( reReplace( arguments.html, "\{ts '[^']*'\}", "", "all" ), 'id="[^"]*"', "", "all" );
	}

	private string function req( required string fn, struct urls={}, string path="" ) {
		var u = duplicate( arguments.urls );
		u.fn = arguments.fn;
		return _InternalRequest(
			template: "#variables.uri#/#arguments.path#dump.cfm",
			urls: u
		).filecontent;
	}

	private string function createURI( string calledName ) {
		var baseURI = "/test/#listLast( getDirectoryFromPath( getCurrentTemplatePath() ), "\/" )#/";
		return baseURI & "" & calledName;
	}

}
