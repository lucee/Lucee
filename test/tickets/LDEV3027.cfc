component extends="org.lucee.cfml.test.LuceeTestCase" labels="security" {

	/*
	 * CVE-2026-29519 / LDEV-3027
	 *
	 * The path of a missing template ends up in the MissingIncludeException message and in
	 * additional.Detail. The exception text stays raw (logs, cfcatch, JSON), the detailed
	 * error templates have to encode it when rendering HTML.
	 */

	variables.payloads = [
		"<img src=x onerror=alert(1)>",
		"<script>alert(document.domain)</script>"
	];

	function run( testResults, testBox ) {

		describe( "LDEV-3027 missinginclude keeps the raw path in the exception", function() {

			it( title="cfcatch message + detail contain the raw <img> path", body=function( currentSpec ) {
				var payload = variables.payloads[ 1 ];
				var caught = false;
				try {
					include template="/#payload#_LDEV3027_does_not_exist.cfm";
				}
				catch ( missinginclude e ) {
					caught = true;
					expect( e.message ).toInclude( payload );
					expect( e.additional.detail ).toInclude( payload );
					expect( e.message ).notToInclude( "&lt;" );
				}
				expect( caught ).toBeTrue( "expected a missinginclude exception" );
			});

			it( title="cfcatch message + detail contain the raw <script> path", body=function( currentSpec ) {
				var payload = variables.payloads[ 2 ];
				var caught = false;
				try {
					include template="/#payload#_LDEV3027_does_not_exist.cfm";
				}
				catch ( missinginclude e ) {
					caught = true;
					expect( e.message ).toInclude( payload );
					expect( e.additional.detail ).toInclude( payload );
				}
				expect( caught ).toBeTrue( "expected a missinginclude exception" );
			});
		});

		describe( "LDEV-3027 detailed error templates must encode the missing path", function() {

			loop array=[ "error.cfm", "error-neo.cfm" ] item="local.errorTemplate" {
				loop array=[ "include", "request" ] item="local.mode" {
					loop array=variables.payloads item="local.payload" {
						it( title="#errorTemplate# / #mode# / #payload#",
							data={ errorTemplate: errorTemplate, mode: mode, payload: payload },
							body=function( data ) {
								var res = renderError( data.payload, data.mode, data.errorTemplate );
								expect( res.type ).toBe( "missinginclude" );
								// the exception itself keeps the raw path (script protect may rewrite a <script> tag in the request uri)
								expect( res.message ).toInclude( data.mode == "request" ? replace( data.payload, "<script>", "" ) : data.payload );
								// the rendered HTML must not contain the markup as a tag
								expect( res.html ).notToInclude( data.payload );
								expect( res.html ).notToInclude( "<img src=x" );
								expect( res.html ).notToInclude( "<script>alert" );
								// but still shows it, encoded
								expect( res.html ).toInclude( find( "<script>", data.payload ) ? "&lt;/script&gt;" : "&lt;img src=x" );
							}
						);
					}
				}
			}
		});

		describe( "LDEV-3027 markdownToHTML safeMode", function() {

			it( title="safeMode=true escapes raw HTML", body=function( currentSpec ) {
				var html = markdownToHTML( "File not found: /a/<img src=x onerror=alert(1)>/b.cfm", true );
				expect( html ).notToInclude( "<img" );
				expect( html ).toInclude( "&lt;img" );

				html = markdownToHTML( "<script>alert(1)</script>", true );
				expect( html ).notToInclude( "<script" );
			});

			it( title="safeMode=true drops javascript: links", body=function( currentSpec ) {
				var html = markdownToHTML( "[click](javascript:alert(1))", true );
				expect( html ).notToInclude( "javascript:" );
			});

			it( title="safeMode=true still renders markdown", body=function( currentSpec ) {
				var html = markdownToHTML( "**bold** and `a<b`", true );
				expect( html ).toInclude( "<strong>bold</strong>" );
				expect( html ).toInclude( "<code>a&lt;b</code>" );
			});

			it( title="safeMode works as member function", body=function( currentSpec ) {
				expect( "<b>x</b>".markdownToHTML( true ) ).notToInclude( "<b>" );
			});

			it( title="default (safeMode=false) is unchanged", body=function( currentSpec ) {
				expect( markdownToHTML( "<b>x</b>" ) ).toInclude( "<b>x</b>" );
				expect( markdownToHTML( "<b>x</b>", false ) ).toInclude( "<b>x</b>" );
			});
		});
	}

	private struct function renderError( required string payload, required string mode, required string errorTemplate ) {
		var result = _internalRequest(
			template: createURI( "LDEV3027/render.cfm" ),
			forms: { payload: arguments.payload, mode: arguments.mode, errorTemplate: arguments.errorTemplate }
		);
		return deserializeJSON( result.filecontent );
	}

	private string function createURI( string calledName ) {
		var baseURI = "/test/#listLast( getDirectoryFromPath( getCurrentTemplatePath() ), "\/" )#/";
		return baseURI & calledName;
	}
}
