component extends="org.lucee.cfml.test.LuceeTestCase" skip=true {

	function run( testResults, testBox ){
		xdescribe( "Test suite for LDEV2038 - return type any", function(){
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

		describe( "Test suite for LDEV2038 - return extended type", function(){
			it( title = "interface method returns extended type", body = function( currentSpec ){
				// returns ldev2038_B which extends ldev2038_A,
				// interface specifies ldev2038_A
				// method specifies returning ldev2038_A
				var obj = new LDEV2038.ldev2038_inherited_type();
				var result = obj.test();
				expect ( result ).toBeComponent();
				debug(result);
			});

			it( title = "interface method returns extended type", body = function( currentSpec ){
				// returns ldev2038_B which extends ldev2038_A,
				// interface specifies ldev2038_A
				// method specifies returning ldev2038_B
				var obj = new LDEV2038.ldev2038_inherited_type_B();
				var result = obj.test();
				expect ( result ).toBeComponent();
				debug(result);
			});
		});
	}

}