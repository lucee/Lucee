component extends="org.lucee.cfml.test.LuceeTestCase" labels="qoq" {

	function beforeAll() {
		variables.qSource = queryNew( "col1,col2", "varchar,varchar", [
			{ col1: "ITEM1", col2: "A" },
			{ col1: "ITEM1", col2: "A" },
			{ col1: "ITEM2", col2: "B" }
		] );
		variables.sessionId = "REPRO-SESSION-1234";
		variables.nativeQoQ = { dbtype: { type: "query", engine: "native" } };
	}

	function run( testResults, testBox ) {

		describe( "LDEV-6976 QoQ DISTINCT/GROUP BY with a query param in the SELECT list", function() {

			it( title="control: param in SELECT list without DISTINCT/GROUP BY", body=function() {
				var qSource = variables.qSource;
				var result = queryExecute(
					"SELECT col1, ? AS tSessionId FROM qSource ORDER BY col1",
					[ { value: variables.sessionId, cfsqltype: "cf_sql_varchar" } ],
					variables.nativeQoQ
				);
				expect( result.recordcount ).toBe( 3 );
				expect( result.tSessionId[ 1 ] ).toBe( variables.sessionId );
				expect( result.tSessionId[ 3 ] ).toBe( variables.sessionId );
			});

			it( title="control: DISTINCT without a param", body=function() {
				var qSource = variables.qSource;
				var result = queryExecute(
					"SELECT DISTINCT col1 FROM qSource ORDER BY col1",
					[],
					variables.nativeQoQ
				);
				expect( result.recordcount ).toBe( 2 );
				expect( result.col1[ 1 ] ).toBe( "ITEM1" );
				expect( result.col1[ 2 ] ).toBe( "ITEM2" );
			});

			it( title="DISTINCT with a positional param in the SELECT list", body=function() {
				var qSource = variables.qSource;
				var result = queryExecute(
					"SELECT DISTINCT col1, ? AS tSessionId FROM qSource ORDER BY col1",
					[ { value: variables.sessionId, cfsqltype: "cf_sql_varchar" } ],
					variables.nativeQoQ
				);
				expect( result.recordcount ).toBe( 2 );
				expect( result.columnList ).toBe( "COL1,TSESSIONID" );
				expect( result.col1[ 1 ] ).toBe( "ITEM1" );
				expect( result.col1[ 2 ] ).toBe( "ITEM2" );
				expect( result.tSessionId[ 1 ] ).toBe( variables.sessionId );
				expect( result.tSessionId[ 2 ] ).toBe( variables.sessionId );
			});

			it( title="DISTINCT with a named param in the SELECT list", body=function() {
				var qSource = variables.qSource;
				var result = queryExecute(
					"SELECT DISTINCT col1, :sid AS tSessionId FROM qSource ORDER BY col1",
					{ sid: { value: variables.sessionId, cfsqltype: "cf_sql_varchar" } },
					variables.nativeQoQ
				);
				expect( result.recordcount ).toBe( 2 );
				expect( result.tSessionId[ 1 ] ).toBe( variables.sessionId );
				expect( result.tSessionId[ 2 ] ).toBe( variables.sessionId );
			});

			it( title="GROUP BY with a param in the SELECT list", body=function() {
				var qSource = variables.qSource;
				var result = queryExecute(
					"SELECT col1, ? AS tSessionId FROM qSource GROUP BY col1 ORDER BY col1",
					[ { value: variables.sessionId, cfsqltype: "cf_sql_varchar" } ],
					variables.nativeQoQ
				);
				expect( result.recordcount ).toBe( 2 );
				expect( result.col1[ 1 ] ).toBe( "ITEM1" );
				expect( result.col1[ 2 ] ).toBe( "ITEM2" );
				expect( result.tSessionId[ 1 ] ).toBe( variables.sessionId );
				expect( result.tSessionId[ 2 ] ).toBe( variables.sessionId );
			});

			it( title="GROUP BY with an aggregate and params in the SELECT list", body=function() {
				var qSource = variables.qSource;
				var result = queryExecute(
					"SELECT col1, ? AS tSessionId, count(*) AS cnt, ? AS num FROM qSource GROUP BY col1 ORDER BY col1",
					[
						{ value: variables.sessionId, cfsqltype: "cf_sql_varchar" },
						{ value: 42, cfsqltype: "cf_sql_integer" }
					],
					variables.nativeQoQ
				);
				expect( result.recordcount ).toBe( 2 );
				expect( result.cnt[ 1 ] ).toBe( 2 );
				expect( result.cnt[ 2 ] ).toBe( 1 );
				expect( result.tSessionId[ 1 ] ).toBe( variables.sessionId );
				expect( result.tSessionId[ 2 ] ).toBe( variables.sessionId );
				expect( result.num[ 1 ] ).toBe( 42 );
				expect( result.num[ 2 ] ).toBe( 42 );
			});

			it( title="cfquery DISTINCT with cfqueryparam in the SELECT list", body=function() {
				```
				<cfquery name="local.result" dbtype="query">
					SELECT DISTINCT col1, <cfqueryparam value="#variables.sessionId#" cfsqltype="cf_sql_varchar"> AS tSessionId
					FROM variables.qSource
					ORDER BY col1
				</cfquery>
				```
				expect( result.recordcount ).toBe( 2 );
				expect( result.tSessionId[ 1 ] ).toBe( variables.sessionId );
				expect( result.tSessionId[ 2 ] ).toBe( variables.sessionId );
			});

		});

	}

}
