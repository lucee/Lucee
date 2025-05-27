component extends="org.lucee.cfml.test.LuceeTestCase" skip=true {

	function run( testResults, testBox ){
		describe( "Test suite for LDEV2352 - return type any", function(){
			it( title = "interface method with any allows return type struct", body = function( currentSpec ){
				var obj = new LDEV2038.ldev2038_struct();
				var result = obj.test();
				expect ( result ).toBeStruct();
			});

			it( title = "interface method with any allows return type cfc", body = function( currentSpec ){
				var obj = new LDEV2038.ldev2038_obj();
				var result = obj.test();
				expect ( result ).toBeComponent();
			});
		});

		describe( "Test suite for LDEV2352 - return extended type", function(){
			// returns ldev2038_B which extends ldev2038_A, interface specifies ldev2038_A
			var obj = new LDEV2038.ldev2038_inherited_type(); 
			var result = obj.test();
			expect ( result ).toBeComponent();
		});
	}

}