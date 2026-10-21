component extends="org.lucee.cfml.test.LuceeTestCase" labels="qoq" {

	function beforeAll() {
		variables.q = queryNew( "navid,type,url", "varchar,decimal,varchar", [
			{ "navid": 200, "type": 1, "url": "offense" },
			{ "navid": 300, "type": 2, "url": "defense" }
		] );
	}

	function run( testResults, testBox ) {
		describe( "LDEV-6543 QoQ cast() inside a sub query", function() {

			it( "cast without a length in a sub query", function() {
				query name="local.res" dbtype="query" {
					echo( "
						select navid, type, url
						from variables.q
						where navid in (
							select cast( navid AS varchar ) as id
							from variables.q
							where type = 1 )
					" );
				}
				expect( res.recordcount ).toBe( 1 );
				expect( res.url ).toBe( "offense" );
			});

			it( "cast with a length in a sub query", function() {
				query name="local.res" dbtype="query" {
					echo( "
						select navid, type, url
						from variables.q
						where left( navid, 3 ) in (
							select cast( navid AS varchar(10) ) as id
							from variables.q
							where type = 2 )
					" );
				}
				expect( res.recordcount ).toBe( 1 );
				expect( res.url ).toBe( "defense" );
			});

			it( "nested cast with a function inside a sub query", function() {
				query name="local.res" dbtype="query" {
					echo( "
						select navid
						from variables.q
						where navid in (
							select cast( lower( cast( navid as varchar(10) ) ) as varchar(20) ) as id
							from variables.q )
						order by navid
					" );
				}
				expect( res.recordcount ).toBe( 2 );
			});

			it( "'cast' inside a string literal is left alone", function() {
				query name="local.res" dbtype="query" {
					echo( "
						select navid
						from variables.q
						where url <> 'cast(x as y)'
						and navid in ( select cast( navid as varchar ) from variables.q where type = 1 )
					" );
				}
				expect( res.recordcount ).toBe( 1 );
			});
		});
	}
}
