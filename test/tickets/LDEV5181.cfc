component extends = "org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults, testBox ){
		describe( "LDEV5181", function(){

			it( "test java.lang.System via createObject", function(){
				var stEnvVars = createObject("java", "java.lang.System").getenv();
			});

			it( "test java.lang.System natively", function(){
				var stEnvVars = java.lang.System::getenv();
			});
		} );
	}

}
