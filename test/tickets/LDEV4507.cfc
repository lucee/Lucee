component extends = "org.lucee.cfml.test.LuceeTestCase" labels="component" skip=true {
	
	function run( testResults, testBox ){
		// all your suites go here.
		describe( "LDEV-4507", function(){

			it( "call getComponentMetaData", function(){
				systemOutput(getComponentMetadata("LDEV4507.test4507simple"));  // regression: Error (java.lang.VerifyError) Message Bad type on operand stack
			} );

			it( "call getComponentMetaData", function(){
				systemOutput(getComponentMetadata("LDEV4507.test4507"));  // regression: Error (java.lang.VerifyError) Message Bad type on operand stack
			} );

		} );
	}

}
