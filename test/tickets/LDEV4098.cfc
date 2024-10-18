component extends="org.lucee.cfml.test.LuceeTestCase" labels="thread" {

	function run( testResults, testBox ){

		describe( "LDEV-498 thread scope content corruption / loss, with testbox 2.3.0 or greater", function(){
			it( title="using `thread` outside a cfthread: simple value", body=function(){
				thread="simple value";
				expect( thread ).toBe( "simple value" );
			} );

			it( title="using `thread` outside a cfthread: collection", body=function(){
				thread.a="A";
				thread.b.c="BC";
				expect( thread.a ).toBe( "A" );
				expect( thread.b.c ).toBe( "BC" );
			} );

			it( title="using `thread` outside a cfthread: simple to collection and back", body=function(){
				thread="simple value1";
				expect( thread ).toBe( "simple value1" );
				thread.a="A";
				expect( thread.a ).toBe( "A" );
				thread="simple value2";
				expect( thread ).toBe( "simple value2" );
			} );


			it( title="using `thread` inside a cfthread", body= function(){
				thread name="test4098_1" action="run" {
					thread.testing = 'blah';
				}
				thread action="join" name="test4098_1";
				expect( cfthread.test4098_1.testing ).toBe( "blah" );
			} );

			it( title="using `thread` in and outside a cfthread", body= function(){
				thread.testing="outside";
				expect( thread.testing ).toBe( "outside" );

				thread name="test4098_2" action="run" {
					thread.testing = 'inside';
				}
				thread action="join" name="test4098_2";
				
				expect( thread.testing ).toBe( "outside" );
				expect( cfthread.test4098_2.testing ).toBe( "inside" );
			} );

		} );
	}

}
