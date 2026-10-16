component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults, testBox ) {
		describe( "LDEV-6424 querySetCell: skip type re-detection when value matches column type", function() {

			it( "typed column reports its declared type after population with matching values", function() {
				var q = queryNew( "id,val", "integer,varchar" );
				for ( var r = 1; r <= 100; r++ ) {
					queryAddRow( q );
					querySetCell( q, "id", r, r );
					querySetCell( q, "val", "row_" & r, r );
				}
				var meta = getMetaData( q );
				expect( meta[ 1 ].typeName ).toBe( "INTEGER" );
				expect( meta[ 2 ].typeName ).toBe( "VARCHAR" );
			});

			it( "typed column re-detects type when populated with non-matching value", function() {
				var q = queryNew( "id", "integer" );
				queryAddRow( q );
				querySetCell( q, "id", "not_a_number", 1 );
				var meta = getMetaData( q );
				// reDefineInteger fails the cast, calls resetType (type=OTHER, typeChecked=false).
				// Next getType triggers reOrganizeType which detects VARCHAR from the stored string.
				expect( meta[ 1 ].typeName ).toBe( "VARCHAR" );
			});

			it( "concurrent getType() on shared typed column returns consistent value with no exceptions", function() {
				var q = queryNew( "id,val", "integer,varchar" );
				for ( var r = 1; r <= 1000; r++ ) {
					queryAddRow( q );
					querySetCell( q, "id", r, r );
					querySetCell( q, "val", "row_" & r, r );
				}

				var results = [];
				var errors  = [];
				var threadCount = 20;
				var readsPerThread = 2000;

				for ( var t = 1; t <= threadCount; t++ ) {
					thread name="reader_#t#" action="run" q=q results=results errors=errors readsPerThread=readsPerThread {
						try {
							var localTypes = [];
							for ( var i = 1; i <= attributes.readsPerThread; i++ ) {
								var m = getMetaData( attributes.q );
								arrayAppend( localTypes, m[ 1 ].typeName & "," & m[ 2 ].typeName );
							}
							for ( var lt in localTypes ) arrayAppend( attributes.results, lt );
						} catch ( any e ) {
							arrayAppend( attributes.errors, e.message );
						}
					}
				}

				for ( var t = 1; t <= threadCount; t++ ) thread action="join" name="reader_#t#";

				expect( arrayLen( errors ) ).toBe( 0, "concurrent readers threw: " & arrayToList( errors, " | " ) );
				expect( arrayLen( results ) ).toBe( threadCount * readsPerThread );

				// every reader should see the same typed pair on every call
				var unique = {};
				for ( var r in results ) unique[ r ] = true;
				expect( structCount( unique ) ).toBe( 1, "expected one unique type pair across all reads, got " & structKeyList( unique ) );
				expect( structKeyExists( unique, "INTEGER,VARCHAR" ) ).toBeTrue();
			});

			it( "concurrent readers + writer with matching values stays type-stable", function() {
				var q = queryNew( "id,val", "integer,varchar" );
				for ( var r = 1; r <= 500; r++ ) {
					queryAddRow( q );
					querySetCell( q, "id", r, r );
					querySetCell( q, "val", "row_" & r, r );
				}

				var readResults = [];
				var errors      = [];
				var readers     = 10;
				var readsPer    = 1000;

				// writer thread keeps overwriting cells with matching-type values during the readers' work
				thread name="writer" action="run" q=q errors=errors {
					try {
						for ( var i = 1; i <= 5000; i++ ) {
							var row = ( ( i - 1 ) mod 500 ) + 1;
							querySetCell( attributes.q, "id",  row * 7, row );
							querySetCell( attributes.q, "val", "u_" & i, row );
						}
					} catch ( any e ) {
						arrayAppend( attributes.errors, "writer: " & e.message );
					}
				}

				for ( var t = 1; t <= readers; t++ ) {
					thread name="r_#t#" action="run" q=q readResults=readResults errors=errors readsPer=readsPer {
						try {
							for ( var i = 1; i <= attributes.readsPer; i++ ) {
								var m = getMetaData( attributes.q );
								arrayAppend( attributes.readResults, m[ 1 ].typeName & "," & m[ 2 ].typeName );
							}
						} catch ( any e ) {
							arrayAppend( attributes.errors, e.message );
						}
					}
				}

				thread action="join" name="writer";
				for ( var t = 1; t <= readers; t++ ) thread action="join" name="r_#t#";

				expect( arrayLen( errors ) ).toBe( 0, "concurrent reader/writer threw: " & arrayToList( errors, " | " ) );

				// every read should observe INTEGER,VARCHAR — matching writes never invalidate the type
				var unique = {};
				for ( var r in readResults ) unique[ r ] = true;
				expect( structCount( unique ) ).toBe( 1, "type drifted during matching writes, observed: " & structKeyList( unique ) );
				expect( structKeyExists( unique, "INTEGER,VARCHAR" ) ).toBeTrue();
			});

		});
	}

}
