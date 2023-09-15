component extends="org.lucee.cfml.test.LuceeTestCase" labels="qoq" {
 
	function beforeAll(){
		variables.q = queryNew("navid, type, url", "varchar,decimal,VarChar",
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
		
			it( title='QoQ error with types and subselect' , body=function() {

				query name="local.res" dbtype="query" {
					echo("
						select 	navid, type, url
						from 	variables.q
						where 	url = 'football'
								and type = 1
								and left( navid, 3 ) in (
									select 	navid
									from 	variables.q
									where 	type = 1
											and url = 'football')
					");
				}
				expect( res.recordcount ).toBe( 1 );
			});

		});

	}

} 