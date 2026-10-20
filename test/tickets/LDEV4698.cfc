component extends="org.lucee.cfml.test.LuceeTestCase" labels="qoq" {

	function beforeAll(){
		variables.q = queryNew("navid, type, url", "varchar,decimal,VarChar",
			[{
					"navid": 200,
					"type": 1,
					"url": "offense"
				}
			]
		);
	};

	function run( testResults , testBox ) {

		describe( title='QofQ' , body=function(){

			it( title='QoQ cast without data length' , body=function() {
				query name="local.res" dbtype="query" {
					echo("
						select 	cast( navid AS varchar ) as id
						from 	variables.q
					");
				}
				expect( res.recordcount ).toBe( 1 );
			});

			it( title='QoQ cast with data length' , body=function() {
				query name="local.res" dbtype="query" {
					echo("
						select 	cast( navid AS varchar(10) ) as id
						from 	variables.q
					");
				}
				expect( res.recordcount ).toBe( 1 );
			});

			it( title='QoQ cast with precision and scale' , body=function() {
				query name="local.res" dbtype="query" {
					echo("
						select 	cast( type AS decimal(10, 2) ) as t, cast( url AS varchar (255) ) as u
						from 	variables.q
					");
				}
				expect( res.recordcount ).toBe( 1 );
				expect( res.t ).toBe( 1 );
				expect( res.u ).toBe( "offense" );
			});

			it( title='QoQ hsqldb cast with data length' , body=function() {

				query name="local.res" dbtype="query" {
					echo("
						select 	cast( q1.navid AS varchar(255) ) as id
						from 	variables.q q1,
								variables.q q2
						where 	q1.type = q2.type
					");
				}
				expect( res.recordcount ).toBe( 1 );
			});

			// the native QoQ parser doesn't support sub queries, so this falls back to HSQLDB (LDEV-6543)
			it( title='QoQ hsqldb cast with data length in sub query' , body=function() {

				query name="local.res" dbtype="query" {
					echo("
						select 	navid, type, url
						from 	variables.q
						where 	url = 'offense'
								and type = 1
								and left( navid, 3 ) in (
									select 	cast( navid AS varchar(10) ) as id
									from 	variables.q
									where 	type = 1
											and url = 'offense')
					");
				}
				expect( res.recordcount ).toBe( 1 );
			});

		});
	}
}