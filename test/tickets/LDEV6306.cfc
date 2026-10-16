component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults, testBox ) {

		describe( "LDEV-6306 REST cross-CFC routing — specificity over disk order", function() {

			beforeEach( function() {
				variables.restUtil = createObject( "java", "lucee.runtime.rest.RestUtil" );
			} );

			describe( "literal vs path-variable", function() {

				it( "literal beats path-var at the same depth", function() {
					expect( restUtil.resolveIndex( [ "/users/{id}", "/users/me" ], "/users/me" ) ).toBe( 1 );
				} );

				it( "literal still wins when listed first", function() {
					expect( restUtil.resolveIndex( [ "/users/me", "/users/{id}" ], "/users/me" ) ).toBe( 0 );
				} );

				it( "path-var matches when no literal alternative exists", function() {
					expect( restUtil.resolveIndex( [ "/users/{id}", "/users/me" ], "/users/123" ) ).toBe( 0 );
				} );

				it( "all literals beat any path-var combination at same depth", function() {
					var patterns = [ "/{a}/{b}/{c}", "/{a}/{b}/c", "/a/b/c" ];
					expect( restUtil.resolveIndex( patterns, "/a/b/c" ) ).toBe( 2 );
				} );

				it( "partial literal mix beats all path-vars", function() {
					var patterns = [ "/{a}/{b}/{c}", "/{a}/b/{c}" ];
					expect( restUtil.resolveIndex( patterns, "/x/b/y" ) ).toBe( 1 );
				} );

				it( "literal in the middle still beats trailing literal alone", function() {
					var patterns = [ "/{a}/b/{c}", "/{a}/{b}/c" ];
					// both have 1 literal segment + 2 free vars, equal score
					// lex tie-break: "/{a}/b/{c}" < "/{a}/{b}/c" because 'b' < '{'
					expect( restUtil.resolveIndex( patterns, "/x/b/c" ) ).toBe( 0 );
				} );

			} );

			describe( "longer match (more segments)", function() {

				it( "longer prefix wins over shorter prefix", function() {
					expect( restUtil.resolveIndex( [ "/api", "/api/v1" ], "/api/v1/ping" ) ).toBe( 1 );
				} );

				it( "longer prefix still wins regardless of pattern order", function() {
					expect( restUtil.resolveIndex( [ "/api/v1", "/api" ], "/api/v1/ping" ) ).toBe( 0 );
				} );

				it( "shorter prefix wins when only it matches", function() {
					expect( restUtil.resolveIndex( [ "/api", "/api/v1" ], "/api/v2/ping" ) ).toBe( 0 );
				} );

				it( "versioned APIs route correctly alongside catch-all", function() {
					var patterns = [ "/api", "/api/v1", "/api/v2" ];
					expect( restUtil.resolveIndex( patterns, "/api/v1/users" ) ).toBe( 1 );
					expect( restUtil.resolveIndex( patterns, "/api/v2/users" ) ).toBe( 2 );
					expect( restUtil.resolveIndex( patterns, "/api/v3/users" ) ).toBe( 0 );
				} );

				it( "longer path-var path beats shorter literal path", function() {
					expect( restUtil.resolveIndex( [ "/a", "/a/{b}/{c}" ], "/a/x/y" ) ).toBe( 1 );
				} );

				it( "shorter literal path beats longer all-vars path of equal segment count", function() {
					// JAX-RS: literal-heavy beats var-heavy. /a (literal) outranks
					// /{x}/{y} (two free vars) for caller /a/b — both match, but
					// the literal segment is more specific.
					expect( restUtil.resolveIndex( [ "/a", "/{x}/{y}" ], "/a/b" ) ).toBe( 0 );
				} );

			} );

			describe( "regex-constrained vs unconstrained path variables", function() {

				it( "regex path-var matches when caller satisfies the constraint", function() {
					expect( restUtil.resolveIndex( [ "/users/{id:[0-9]+}" ], "/users/123" ) ).toBe( 0 );
				} );

				it( "regex path-var rejects callers that violate the constraint", function() {
					expect( restUtil.resolveIndex( [ "/users/{id:[0-9]+}" ], "/users/abc" ) ).toBe( -1 );
				} );

				it( "constrained regex outranks unconstrained path-var", function() {
					var patterns = [ "/users/{id:[0-9]+}", "/users/{id}" ];
					expect( restUtil.resolveIndex( patterns, "/users/123" ) ).toBe( 0 );
				} );

				it( "constrained regex outranks unconstrained even when listed second", function() {
					var patterns = [ "/users/{id}", "/users/{id:[0-9]+}" ];
					expect( restUtil.resolveIndex( patterns, "/users/123" ) ).toBe( 1 );
				} );

				it( "unconstrained path-var wins when constrained regex does not match", function() {
					var patterns = [ "/users/{id:[0-9]+}", "/users/{id}" ];
					expect( restUtil.resolveIndex( patterns, "/users/abc" ) ).toBe( 1 );
				} );

				it( "literal still beats constrained regex", function() {
					var patterns = [ "/users/me", "/users/{id:[0-9]+}", "/users/{id}" ];
					// /users/me requires segment "me", does not match "123"
					expect( restUtil.resolveIndex( patterns, "/users/123" ) ).toBe( 1 );
					// /users/me does match "me" though
					expect( restUtil.resolveIndex( patterns, "/users/me" ) ).toBe( 0 );
				} );

			} );

			describe( "tie-break", function() {

				it( "duplicate patterns: first listed wins", function() {
					expect( restUtil.resolveIndex( [ "/dup", "/dup" ], "/dup/x" ) ).toBe( 0 );
				} );

				it( "equal-specificity different vars: lex-smaller raw path wins", function() {
					// both /users/{id} and /users/{name} score equally and both match /users/123;
					// /users/{id} sorts before /users/{name} lex (because 'i' < 'n')
					expect( restUtil.resolveIndex( [ "/users/{id}", "/users/{name}" ], "/users/123" ) ).toBe( 0 );
					expect( restUtil.resolveIndex( [ "/users/{name}", "/users/{id}" ], "/users/123" ) ).toBe( 1 );
				} );

			} );

			describe( "findDuplicates — case 4 registration validation", function() {

				it( "returns empty list when all patterns are unique", function() {
					expect( restUtil.findDuplicates( [ "/a", "/b", "/c" ] ).size() ).toBe( 0 );
				} );

				it( "returns empty list for empty input", function() {
					expect( restUtil.findDuplicates( [] ).size() ).toBe( 0 );
				} );

				it( "detects a single duplicated path", function() {
					var dupes = restUtil.findDuplicates( [ "/dup", "/other", "/dup" ] );
					expect( dupes.size() ).toBe( 1 );
					expect( dupes.get( 0 ) ).toBe( "/dup" );
				} );

				it( "reports each duplicated path only once even if it appears 3+ times", function() {
					var dupes = restUtil.findDuplicates( [ "/dup", "/dup", "/dup" ] );
					expect( dupes.size() ).toBe( 1 );
					expect( dupes.get( 0 ) ).toBe( "/dup" );
				} );

				it( "reports multiple distinct duplicates in first-seen order", function() {
					var dupes = restUtil.findDuplicates( [ "/a", "/b", "/a", "/c", "/b" ] );
					expect( dupes.size() ).toBe( 2 );
					expect( dupes.get( 0 ) ).toBe( "/a" );
					expect( dupes.get( 1 ) ).toBe( "/b" );
				} );

			} );

			describe( "no match / empty input", function() {

				it( "no patterns match returns -1", function() {
					expect( restUtil.resolveIndex( [ "/users/me", "/api/v1" ], "/orders" ) ).toBe( -1 );
				} );

				it( "empty pattern list returns -1", function() {
					expect( restUtil.resolveIndex( [], "/any" ) ).toBe( -1 );
				} );

				it( "caller path shorter than pattern returns -1", function() {
					expect( restUtil.resolveIndex( [ "/users/{id}/posts" ], "/users/me" ) ).toBe( -1 );
				} );

			} );

		} );
	}

}
