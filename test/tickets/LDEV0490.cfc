component extends="org.lucee.cfml.test.LuceeTestCase"	{

	function run( testResults , testBox ) {

		describe('LDEV-490 ',function(){

			it( 'component with persistent property can be created' , function() {

				actual = new LDEV0490.haspersistent();

				actual.setID( 'walrus' );

				expect( actual.getID() ).toBe( 'walrus' );
				
			});

		});

	}
} 
