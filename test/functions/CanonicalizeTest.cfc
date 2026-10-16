/**
 * TestBox test suite for Lucee canonicalize() regression
 *
 * Bug: canonicalize() strips tab, carriage return, and newline characters
 *      when using OWASP Guard extension 3.0.0.13-RC (default in Lucee 7.0.2.41+).
 *      Previously these whitespace delimiters were preserved.
 *
 * Expected behaviour (pre-3.0.0.13-RC / Lucee 6 / ACF):
 *   - canonicalize() should only decode/normalize encodings iteratively.
 *   - It should NOT filter or strip characters — that is the job of validation.
 *   - Tabs (\t), carriage returns (\r), newlines (\n), backslash (\),
 *     caret (^), and non-ASCII Unicode characters should be preserved.
 */
component displayName="CanonicalizeWhitespaceTest" extends="org.lucee.cfml.test.LuceeTestCase" {

	function run() {

		describe( "canonicalize() whitespace / delimiter preservation", function() {

			// ----------------------------------------------------------------
			// Core regression: tab, CR, LF must survive canonicalize()
			// ----------------------------------------------------------------

			it( "preserves a tab character between tokens", function() {
				var input    = "ZYDGLHM6" & chr(9) & "ZYDGLC8G";
				var expected = input; // must be unchanged
				expect( canonicalize( input, false, false ) ).toBe( expected );
			} );

			it( "preserves a carriage return between tokens", function() {
				var input    = "ZYDGLC8G" & chr(13) & "ZYDGDTWB";
				var expected = input;
				expect( canonicalize( input, false, false ) ).toBe( expected );
			} );

			it( "preserves a newline between tokens", function() {
				var input    = "ZYDGLC8G" & chr(10) & "ZYDGDTWB";
				var expected = input;
				expect( canonicalize( input, false, false ) ).toBe( expected );
			} );

			it( "preserves tab AND carriage return in a multi-token string (image reproduction)", function() {
				// Reproduces the exact scenario shown in the bug screenshots:
				//   ZYDGLHM6<tab>ZYDGLC8G<CR>ZYDGDTWB
				var input    = "ZYDGLHM6" & chr(9) & "ZYDGLC8G" & chr(13) & "ZYDGDTWB";
				var expected = input;
				expect( canonicalize( input, false, false ) ).toBe( expected );
			} );

			// ----------------------------------------------------------------
			// Additional characters reported as stripped by simplify()
			// ----------------------------------------------------------------

			it( "preserves non-ASCII Unicode characters (e.g. accented letters)", function() {
				var input    = "café";
				var expected = "café"; // must NOT become "caf"
				expect( canonicalize( input, false, false ) ).toBe( expected );
			} );

			// ----------------------------------------------------------------
			// Sanity checks — canonicalize() should still decode encodings
			// ----------------------------------------------------------------

			it( "decodes a percent-encoded space to a literal space", function() {
				expect( canonicalize( "%20", false, false ) ).toBe( " " );
			} );

			it( "decodes HTML entity &amp; to &", function() {
				expect( canonicalize( "&amp;", false, false ) ).toBe( "&" );
			} );

			it( "leaves a plain alphanumeric string unchanged", function() {
				var input = "HelloWorld123";
				expect( canonicalize( input, false, false ) ).toBe( input );
			} );

		} );

	}

}
