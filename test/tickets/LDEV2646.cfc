component extends = "org.lucee.cfml.test.LuceeTestCase" labels="syntax" {
	function beforeAll(){
		pagePoolClear();
	}

	function run( testResults, testBox ){
		describe( "Test case for LDEV2646", function(){
			it( title = "checking lock with comment line" , body = function( currentSpec ){
				var obj = createobject( "component", "LDEV2646.obj" );
				debug(obj);
				expect( obj.testBadDocComment() ).toBeTrue();
				
			});
			
			it( title = "checking lock without comment line" , body = function( currentSpec ){
				var obj = createobject( "component", "LDEV2646.obj" );
				debug(obj.testGoodDocComment);
				expect( obj.testGoodDocComment() ).toBeTrue();
			});

			it( title = "checking lock no comment line" , body = function( currentSpec ){
				var obj = createobject( "component", "LDEV2646.obj" );
				expect( obj.testNoDocComment() ).toBeTrue();
			});
		});
	}

}