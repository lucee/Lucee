component extends = "org.lucee.cfml.test.LuceeTestCase" labels="qoq" {

	function run( testResults, testBox ){
		// all your suites go here.
		describe( "test qoq support with $", function(){

			it( "test native qoq column names ", function(){

				var q = querynew( "id$lucee" );
				queryAddRow( q );
				querySetCell( q, "id$lucee", 3 );
				query name="local.r" dbtype="query" {
					echo( "select id$lucee from q" );
				}
				expect( r[ "id$lucee" ] ).toBe( 3 );
				expect( r.recordcount ).toBe( 1 );

			});


			it( "test native qoq with $ in column and leading a table name", function(){

				var $q = querynew( "id$lucee" );
				queryAddRow( $q );
				querySetCell( $q, "id$lucee", 3 );
				query name="local.r" dbtype="query" {
					echo( "select id$lucee from $q" );
				}
				expect( r[ "id$lucee" ] ).toBe( 3 );
				expect( r.recordcount ).toBe( 1 );

			});

			it( "test native qoq with $ in column name and $ inside a table name", function(){

				var q$1 = querynew( "id$lucee" );
				queryAddRow( q$1 );
				querySetCell( q$1, "id$lucee", 3 );
				query name="local.r" dbtype="query" {
					echo( "select id$lucee from q$1" );
				}
				expect( r[ "id$lucee" ] ).toBe( 3 );
				expect( r.recordcount ).toBe( 1 );

			});

			it( "test native qoq with $ in column name and _ leading a table name", function(){

				var _q = querynew( "id$lucee" );
				queryAddRow( _q );
				querySetCell( _q, "id$lucee", 3 );
				query name="local.r" dbtype="query" {
					echo( "select id$lucee from _q" );
				}
				expect( r[ "id$lucee" ] ).toBe( 3 );
				expect( r.recordcount ).toBe( 1 );

			});


			it( "test hsqldb qoq with $ in column names ", function(){

				var q = querynew( "id$lucee" );
				queryAddRow( q );
				querySetCell( q, "id$lucee", 4 );
				query name="local.r" dbtype="query" {
					echo( "select q1.id$lucee from q q1, q q2 where q1.id$lucee = q2.id$lucee" ); // join to force hsqldb
				}

				expect( r[ "id$lucee" ] ).toBe( 4 );
				expect( r.recordcount ).toBe( 1 );

			});

			it( "test hsqldb qoq with $ in column name and $ leading table name", function(){

				var $q = querynew( "id$lucee" );
				queryAddRow( $q );
				querySetCell( $q, "id$lucee", 4 );
				query name="local.r" dbtype="query" {
					echo( "select q1.id$lucee from $q q1, $q q2 where q1.id$lucee = q2.id$lucee" ); // join to force hsqldb
				}

				expect( r[ "id$lucee" ] ).toBe( 4 );
				expect( r.recordcount ).toBe( 1 );

			});

			it( "test hsqldb qoq with $ in column name and _ leading table name", function(){

				var _q = querynew( "id$lucee" );
				queryAddRow( _q );
				querySetCell( _q, "id$lucee", 4 );
				query name="local.r" dbtype="query" {
					echo( "select q1.id$lucee from _q q1, _q q2 where q1.id$lucee = q2.id$lucee" ); // join to force hsqldb
				}

				expect( r[ "id$lucee" ] ).toBe( 4 );
				expect( r.recordcount ).toBe( 1 );

			});

			it( "test hsqldb qoq with $ in column name and $ in table name", function(){

				var q$1 = querynew( "id$lucee" );
				queryAddRow( q$1 );
				querySetCell( q$1, "id$lucee", 4 );
				query name="local.r" dbtype="query" {
					echo( "select q1.id$lucee from q$1 q1, q$1 q2 where q1.id$lucee = q2.id$lucee" ); // join to force hsqldb
				}

				expect( r[ "id$lucee" ] ).toBe( 4 );
				expect( r.recordcount ).toBe( 1 );

			});

		} );
	}

}
