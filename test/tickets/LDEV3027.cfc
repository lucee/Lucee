component extends="org.lucee.cfml.test.LuceeTestCase" labels="security" {

	/*
	 * CVE-2026-29519 / LDEV-3027 — reflected XSS via HTML in the request path.
	 *
	 * When a requested template cannot be found, Lucee raises a
	 * MissingIncludeException whose message and detail embed the requested
	 * path (PageSource.getRealpathWithVirtual() / getDisplayPath()). That
	 * text is reflected into the (HTML) detailed error page unescaped, so an
	 * attacker-supplied path segment such as
	 *     /foo/<img src=x onerror=alert(1)>/index.cfm/
	 * executes as live markup in the victim's browser.
	 *
	 * Fix: escape the requested path where it enters the exception message
	 * and detail (MissingIncludeException), mirroring the existing escaping
	 * of the REST 404 path in PageContextImpl. These tests assert the raw
	 * markup never survives into message/detail and that the escaped form is
	 * present instead.
	 */

	function run( testResults, testBox ) {

		describe( "CVE-2026-29519: MissingInclude must HTML-escape the requested path", function() {

			it( title = "message + detail must not contain raw <img onerror> markup", body = function( currentSpec ) {
				var payload = "<img src=x onerror=alert(1)>";
				var caught  = false;
				try {
					// Non-existent template whose path carries the payload — this
					// is exactly what the request-path vector produces internally.
					include template = "/#payload#_LDEV3027_does_not_exist.cfm";
				}
				catch ( missinginclude e ) {
					caught = true;
					// Only a RAW tag-open is dangerous. Escaping turns "<" into "&lt;",
					// which neutralises the XSS; the attribute text "onerror=" legitimately
					// survives inside the escaped "&lt;img ... onerror=...&gt;" and must NOT
					// be asserted against (that was a false-positive in an earlier draft).
					expect( e.message ).notToInclude( "<img" );
					expect( e.detail  ).notToInclude( "<img" );
					// the path is still reported, just neutralised
					expect( e.message & " " & e.detail ).toInclude( "&lt;img" );
				}
				expect( caught ).toBeTrue( "expected a missinginclude exception" );
			});

			it( title = "script-tag payload must be escaped in message + detail", body = function( currentSpec ) {
				var payload = "<script>alert(document.domain)</script>";
				var caught  = false;
				try {
					include template = "/#payload#_LDEV3027_does_not_exist.cfm";
				}
				catch ( missinginclude e ) {
					caught = true;
					expect( e.message ).notToInclude( "<script>" );
					expect( e.detail  ).notToInclude( "<script>" );
					expect( e.message & " " & e.detail ).toInclude( "&lt;script&gt;" );
				}
				expect( caught ).toBeTrue( "expected a missinginclude exception" );
			});

		});
	}
}
