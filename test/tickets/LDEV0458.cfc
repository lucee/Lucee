component extends="org.lucee.cfml.test.LuceeTestCase"	{

	function run( testResults , testBox ) {

		describe('LDEV-458 test component',function(){

			beforeEach( function() {
				actual = GetMetadata( new LDEV0458.hasparams() ).properties;
			});

			it( 'has 3 properties' , function() {
				expect( actual ).toBeArray();
				expect( actual ).toHaveLength( 3 );
			});

			it( 'has expected first property' , function() {
				expect( actual[1] ).toBe( {
					'type': 'any',
					'name': 'property1',
					'inject': ''
				} );
			});

			it( 'has expected second property' , function() {
				expect( actual[2] ).toBe( {
					'type': 'any',
					'name': 'property2',
					'inject': 'property2'
				} );
			});

			it( 'has expected third property' , function() {
				expect( actual[3] ).toBe( {
					'type': 'any',
					'name': 'property3',
					'inject': 'something_else'
				} );
			});


		});
	}
} 
