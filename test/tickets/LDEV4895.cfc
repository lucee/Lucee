component extends="org.lucee.cfml.test.LuceeTestCase" labels="qoq" {
 

	function run( testResults , testBox ) {

		describe( title='LDEV-4895' , body=function(){
		
			it( title='test single tenary' , body=function() {
				var a =  true ? 1 : 2;
				expect( a ).toBe( 1 );
			});

			it( title='test double tenary, no bracket' , body=function() {
				var a = true ? true ? 1 : 2 : 3;
				expect( a ).toBe( 1 );
				var a = true ? false ? 1 : 2 : 3;
				expect( a ).toBe( 2 );
				var a = false ? true ? 1 : 2 : 3;
				expect( a ).toBe( 3 );
			});

			it( title='test double tenary with bracket' , body=function() {
				var a = true ? (true ? 1 : 2) : 3;
				expect( a ).toBe( 1 );
				var a = true ? (false ? 1 : 2) : 3;
				expect( a ).toBe( 2 );
				var a = false ? (true ? 1 : 2) : 3;
				expect( a ).toBe( 3 );
			});

			it( title='test double tenary with bracket and function calls' , body=function() {
				var a = true ? true ? int(1) : 2 : 3;
				expect( a ).toBe( 1 );
				var a = true ? false ? 1 : int(2) : 3;
				expect( a ).toBe( 2 );
				var a = false ? true ? int(1) : int(2) : int(3);
				expect( a ).toBe( 3 );
			});


		});

	}

} 