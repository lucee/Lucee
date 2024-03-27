component extends="org.lucee.cfml.test.LuceeTestCase" labels="qoq" {
 
	function beforeAll(){
		
		variables.q1 = queryNew("navid, type, url", "decimal,decimal,VarChar",
			[{
					"navid": 200,
					"type": 1,
					"url": "football"
				}
			]
		);
	};

	function run( testResults , testBox ) {

		describe( title='QofQ' , body=function(){
		
			it( title='QoQ with same types as sub select' , body=function() {
				// note this is with a varchar for navid, same type as left() output
				var q = queryNew("navid, type, url", "varchar,decimal,VarChar", 
					[{
							"navid": 200,
							"type": 1,
							"url": "football"
						}
					]
				);

				query name="local.res" dbtype="query" {
					echo("
						select 	navid, type, url
						from 	q
						where 	url = 'football'
								and type = 1
								and left( navid, 3 ) in (
									select 	navid
									from 	q
									where 	type = 1
											and url = 'football')
					");
				}
				expect( res.recordcount ).toBe( 1 );
			});

			it( title='QoQ errors with different types and sub select', skip=true, body=function() {
				// note this is with a decimal for navid, DIFFERENT type as left() output (string)
				var q = queryNew("navid, type, url", "decimal,decimal,VarChar", 
					[{
							"navid": 200,
							"type": 1,
							"url": "football"
						}
					]
				);
				query name="local.res" dbtype="query" {
					echo("
						select 	navid, type, url
						from 	q
						where 	url = 'football'
								and type = 1
								and left( navid, 3 ) in (
									select 	navid
									from 	q
									where 	type = 1
											and url = 'football')
					");
				} // throws incompatible data type in conversion
				expect( res.recordcount ).toBe( 1 );
			});

		});

	}

} 