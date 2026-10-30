component extends="org.lucee.cfml.test.LuceeTestCase" labels="whitespace" {

	function run( testResults, testBox ) {
		describe( "LDEV-3002 / LDEV-670 whitespace management per application and per request", function() {

			var NL = chr( 10 );

			describe( "this.whitespaceManagement in Application.cfc", function() {

				it( title="regular does not touch the whitespace", body=function( currentSpec ) {
					expect( req( "regular/page.cfm" ) ).toBe( "a    b" & NL & NL & NL & "c<pre>x    y</pre>" );
				});

				it( title="white-space removes consecutive whitespace", body=function( currentSpec ) {
					expect( req( "simple/page.cfm" ) ).toBe( "a b" & NL & "c<pre>x y</pre>" );
				});

				it( title="white-space-pref removes consecutive whitespace except within pre", body=function( currentSpec ) {
					expect( req( "smart/page.cfm" ) ).toBe( "a b" & NL & "c<pre>x    y</pre>" );
				});

				it( title="the setting is returned by getApplicationSettings", body=function( currentSpec ) {
					expect( req( "regular/settings.cfm" ) ).toBe( "regular" );
					expect( req( "simple/settings.cfm" ) ).toBe( "white-space" );
					expect( req( "smart/settings.cfm" ) ).toBe( "white-space-pref" );
				});

				it( title="an invalid value throws an error", body=function( currentSpec ) {
					expect( function() {
						req( "invalid/invalid.cfm" );
					}).toThrow();
				});

				it( title="savecontent trim follows the application setting", body=function( currentSpec ) {
					// without trim attribute savecontent trims when whitespace management is on
					expect( req( "simple/savecontent.cfm", { mode: "simple" } ) ).toBe( "[x y][x y]" );
					expect( req( "regular/savecontent.cfm", { mode: "regular" } ) ).toBe( "[  x    y  ][  x    y  ]" );
				});

			});

			describe( "cfsetting whitespaceManagement per request", function() {

				it( title="switch from regular to white-space for the rest of the request", body=function( currentSpec ) {
					expect( req( "regular/request.cfm", { mode: "white-space" } ) ).toBe( "a    bc d" );
					expect( req( "regular/request.cfm", { mode: "simple" } ) ).toBe( "a    bc d" );
				});

				it( title="switch from white-space to regular for the rest of the request", body=function( currentSpec ) {
					expect( req( "simple/request.cfm", { mode: "regular" } ) ).toBe( "a bc    d" );
					expect( req( "simple/request.cfm", { mode: "normal" } ) ).toBe( "a bc    d" );
				});

				it( title="switch to white-space-pref", body=function( currentSpec ) {
					expect( req( "regular/request.cfm", { mode: "white-space-pref" } ) ).toBe( "a    bc d" );
					expect( req( "regular/request.cfm", { mode: "smart" } ) ).toBe( "a    bc d" );
				});

				it( title="the same value as the current one changes nothing", body=function( currentSpec ) {
					expect( req( "simple/request.cfm", { mode: "white-space" } ) ).toBe( "a bc d" );
					expect( req( "regular/request.cfm", { mode: "regular" } ) ).toBe( "a    bc    d" );
				});

				it( title="works as tag", body=function( currentSpec ) {
					expect( req( "regular/tag.cfm", { mode: "simple" } ).trim() ).toBe( "a    bc d" );
					expect( req( "simple/tag.cfm", { mode: "regular" } ).trim() ).toBe( "a bc    d" );
				});

				it( title="works after the response was flushed", body=function( currentSpec ) {
					expect( req( "regular/flush.cfm", { mode: "simple" } ) ).toBe( "a    bc d" );
					expect( req( "simple/flush.cfm", { mode: "regular" } ) ).toBe( "a bc    d" );
				});

				it( title="works inside a savecontent, output written so far is not lost", body=function( currentSpec ) {
					// the content of the savecontent itself is not processed, the processing happens when it is written to the response
					expect( req( "regular/nested.cfm", { mode: "simple" } ) ).toBe( "[p qr s]t u" );
					expect( req( "simple/nested.cfm", { mode: "regular" } ) ).toBe( "[p    qr    s]t    u" );
				});

				it( title="savecontent trim follows the request setting", body=function( currentSpec ) {
					expect( req( "regular/savecontent.cfm", { mode: "simple" } ) ).toBe( "[  x    y  ][x y]" );
					expect( req( "simple/savecontent.cfm", { mode: "regular" } ) ).toBe( "[x y][  x    y  ]" );
				});

				it( title="the setting of the request does not leak into the next request", body=function( currentSpec ) {
					req( "regular/request.cfm", { mode: "simple" } );
					expect( req( "regular/page.cfm" ) ).toBe( "a    b" & NL & NL & NL & "c<pre>x    y</pre>" );
					req( "simple/request.cfm", { mode: "regular" } );
					expect( req( "simple/page.cfm" ) ).toBe( "a b" & NL & "c<pre>x y</pre>" );
				});

				it( title="an invalid value throws an error", body=function( currentSpec ) {
					expect( function() {
						req( "regular/badvalue.cfm" );
					}).toThrow();
				});

			});

		});
	}

	private string function req( required string page, struct url={} ) {
		return _InternalRequest( template: createURI( "LDEV3002/" & arguments.page ), url: arguments.url ).fileContent.trim();
	}

	private string function createURI( string calledName ) {
		var baseURI = "/test/#listLast( getDirectoryFromPath( getCurrentTemplatePath() ), "\/" )#/";
		return baseURI & calledName;
	}

}
