component extends="org.lucee.cfml.test.LuceeTestCase" labels="query" {

	variables.ds = server.getDatasource( service="h2", dbFile=server._getTempDir( "LDEV4867" ) );

	function run( testResults , testBox ) {
		describe( title='LDEV-4867' , body=function(){

			// --- original repro: comments must survive on the SQL sent to the datasource ---
			it( title='test query parsing, removing comments' , body=function() {
				```
				<cfquery name="local.test" datasource="#ds#" result="local.result">
					-- foo
					/* bar */
						SELECT 'test'
				</cfquery>
				```
				//systemOutput( local.result.sql, true );
				expect ( local.result.sql ).toInclude( "-- foo" );
				expect ( local.result.sql ).toInclude( "/* bar */" );
			});

			it( title='test query parsing, with a ? in a comment' , body=function() {
				```
				<cfquery name="local.test" datasource="#ds#" result="local.result">
					-- foo
					/* bar? */
						SELECT 'test'
				</cfquery>
				```

				//systemOutput( local.result.sql, true );
				expect ( local.result.sql ).toInclude("-- foo");
				expect ( local.result.sql ).toInclude("/* bar? */");
			});

			// --- block + line + trailing comments are all preserved verbatim ---
			it( title='block, line and trailing comments are preserved in result.sql', body=function() {
				query name="local.test" datasource="#ds#" result="local.result" {
					echo("
					-- lead line comment
					/* block comment */
					SELECT 'x' AS val /* trailing block */
					");
				}
				expect( local.result.sql ).toInclude( "-- lead line comment" );
				expect( local.result.sql ).toInclude( "/* block comment */" );
				expect( local.result.sql ).toInclude( "/* trailing block */" );
			});

			// --- a trailing single line comment with no newline must not break the parser (LDEV-4866 bounds) ---
			it( title='trailing line comment with no newline does not break parsing', body=function() {
				query name="local.test" datasource="#ds#" result="local.result" {
					echo("SELECT 'ok' AS val -- trailing, no newline");
				}
				expect( local.test.val ).toBe( "ok" );
				expect( local.result.sql ).toInclude( "-- trailing, no newline" );
			});

			// --- ? and : inside comments must NOT be treated as parameters (QoQ path) ---
			it( title='? and : inside comments are not counted as parameters', body=function() {
				var qry = queryNew( "id,engine", "integer,varchar", [
					[ 1, "lucee" ],
					[ 2, "railo" ]
				]);
				// if the ? / :nope inside the comments were parsed as params, this would throw
				var q = queryExecute("
					/* is this a param? :nope and ? */
					-- trailing ? :neither
					SELECT engine FROM qry WHERE id = :id
				", { id: 1 }, { dbtype: "query" });
				expect( q.recordcount ).toBe( 1 );
				expect( q.engine ).toBe( "lucee" );
			});

			// --- REGRESSION GUARD: comment markers INSIDE a string literal must be preserved, ---
			// --- i.e. the QoQ comment stripper must be quote aware (a naive stripper corrupts these) ---
			it( title='comment markers inside string literals are not stripped (QoQ)', body=function() {
				var qry = queryNew( "id,note", "integer,varchar", [
					[ 1, "a--b" ],
					[ 2, "c/*d*/e" ]
				]);

				// '--' inside a literal is data, not a line comment
				var q1 = queryExecute( "SELECT id FROM qry WHERE note = 'a--b'", {}, { dbtype: "query" } );
				expect( q1.recordcount ).toBe( 1 );
				expect( q1.id ).toBe( 1 );

				// '/* */' inside a literal is data, not a block comment
				var q2 = queryExecute( "SELECT id FROM qry WHERE note = 'c/*d*/e'", {}, { dbtype: "query" } );
				expect( q2.recordcount ).toBe( 1 );
				expect( q2.id ).toBe( 2 );
			});

			// --- real comments around a QoQ still work end to end (stripped for the parser) ---
			it( title='comments around a QoQ are stripped for the internal parser', body=function() {
				var qry = queryNew( "id,engine", "integer,varchar", [
					[ 1, "lucee" ]
				]);
				var q = queryExecute("
					/* pick the engine */
					SELECT engine
					FROM qry -- from the in memory query
					WHERE id = 1
				", {}, { dbtype: "query" });
				expect( q.engine ).toBe( "lucee" );
			});
		});
	}

}
