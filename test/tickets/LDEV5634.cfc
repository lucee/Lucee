component extends="org.lucee.cfml.test.LuceeTestCase" labels="qoq" {

	function beforeAll() {
		variables.news = queryNew( "id,title,status", "integer,varchar,varchar" );
		queryAddRow( news );
		querySetCell( news, "id", 1 );
		querySetCell( news, "title", "Dewey defeats Truman" );
		querySetCell( news, "status", "published" );
		queryAddRow( news );
		querySetCell( news, "id", 2 );
		querySetCell( news, "title", "Men walk on Moon" );
		querySetCell( news, "status", javaCast( "null", "" ) );
	}

	function run( testResults, testBox ) {
		describe( title="LDEV-5634: QoQ should error on non-existent columns", body=function() {

			it( title="WHERE clause with non-existent column should throw", body=function( currentSpec ) {
				expect( function() {
					```
					<cfquery name="local.q" dbtype="query">
						SELECT *
						FROM news
						WHERE bob = 1
					</cfquery>
					```
				} ).toThrow( "", "", "column [bob] not found" );
			} );

			it( title="SELECT with non-existent column should throw", body=function( currentSpec ) {
				expect( function() {
					```
					<cfquery name="local.q" dbtype="query">
						SELECT bob
						FROM news
					</cfquery>
					```
				} ).toThrow( "", "", "column [bob] not found" );
			} );

			it( title="ORDER BY with non-existent column should throw", body=function( currentSpec ) {
				expect( function() {
					```
					<cfquery name="local.q" dbtype="query">
						SELECT *
						FROM news
						ORDER BY bob
					</cfquery>
					```
				} ).toThrow( "", "", "column [bob] not found" );
			} );

			it( title="GROUP BY with non-existent column should throw", body=function( currentSpec ) {
				expect( function() {
					```
					<cfquery name="local.q" dbtype="query">
						SELECT id
						FROM news
						GROUP BY bob
					</cfquery>
					```
				} ).toThrow( "", "", "column [bob] not found" );
			} );

			it( title="HAVING with non-existent column should throw", body=function( currentSpec ) {
				expect( function() {
					```
					<cfquery name="local.q" dbtype="query">
						SELECT id, count(*) as cnt
						FROM news
						GROUP BY id
						HAVING bob > 1
					</cfquery>
					```
				} ).toThrow( "", "", "column [bob] not found" );
			} );

		} );

		describe( title="LDEV-5634: QoQ IS NULL/IS NOT NULL should still work", body=function() {

			it( title="IS NULL with valid column should work", body=function( currentSpec ) {
				```
				<cfquery name="local.q" dbtype="query">
					SELECT *
					FROM news
					WHERE status IS NULL
				</cfquery>
				```
				expect( q.recordCount ).toBe( 1 );
				expect( q.id ).toBe( 2 );
			} );

			it( title="IS NOT NULL with valid column should work", body=function( currentSpec ) {
				```
				<cfquery name="local.q" dbtype="query">
					SELECT *
					FROM news
					WHERE status IS NOT NULL
				</cfquery>
				```
				expect( q.recordCount ).toBe( 1 );
				expect( q.id ).toBe( 1 );
			} );

			it( title="valid WHERE clause should still work", body=function( currentSpec ) {
				```
				<cfquery name="local.q" dbtype="query">
					SELECT *
					FROM news
					WHERE id = 1
				</cfquery>
				```
				expect( q.recordCount ).toBe( 1 );
				expect( q.title ).toBe( "Dewey defeats Truman" );
			} );

		} );
	}

}