component extends="org.lucee.cfml.test.LuceeTestCase" labels="qoq" {

	function beforeAll() {
		variables.src = queryNew( "id,num", "integer,double" );
		queryAddRow( variables.src );
		querySetCell( variables.src, "id", 1 );
		querySetCell( variables.src, "num", 25 );
		queryAddRow( variables.src );
		querySetCell( variables.src, "id", 2 );
		querySetCell( variables.src, "num", 16 );
		queryAddRow( variables.src );
		querySetCell( variables.src, "id", 3 );
		querySetCell( variables.src, "num", 0 );
		queryAddRow( variables.src );
		querySetCell( variables.src, "id", 4 );
		querySetCell( variables.src, "num", 1 );

		variables.nativeQoQ = { dbtype: { type: "query", engine: "native" } };
	}

	function run( testResults, testBox ) {

		describe( title="LDEV-6153 native QoQ sqrt() is broken", body=function() {

			it( title="sqrt() returns correct value", body=function() {
				var src = variables.src;
				```
				<cfquery name="local.q" attributeCollection="#variables.nativeQoQ#">
					SELECT sqrt( num ) AS result FROM src WHERE id = 1
				</cfquery>
				```
				expect( q.result ).toBe( 5 );
			});

			it( title="sqrt() works with zero", body=function() {
				var src = variables.src;
				```
				<cfquery name="local.q" attributeCollection="#variables.nativeQoQ#">
					SELECT sqrt( num ) AS result FROM src WHERE id = 3
				</cfquery>
				```
				expect( q.result ).toBe( 0 );
			});

			it( title="sqrt() works with 1", body=function() {
				var src = variables.src;
				```
				<cfquery name="local.q" attributeCollection="#variables.nativeQoQ#">
					SELECT sqrt( num ) AS result FROM src WHERE id = 4
				</cfquery>
				```
				expect( q.result ).toBe( 1 );
			});

			it( title="sqrt() in expression", body=function() {
				var src = variables.src;
				```
				<cfquery name="local.q" attributeCollection="#variables.nativeQoQ#">
					SELECT sqrt( num ) + 1 AS result FROM src WHERE id = 2
				</cfquery>
				```
				expect( q.result ).toBe( 5 );
			});

			it( title="sqrt() in WHERE clause", body=function() {
				var src = variables.src;
				```
				<cfquery name="local.q" attributeCollection="#variables.nativeQoQ#">
					SELECT id FROM src WHERE sqrt( num ) = 5
				</cfquery>
				```
				expect( q.recordcount ).toBe( 1 );
				expect( q.id ).toBe( 1 );
			});

		});

	}

}
