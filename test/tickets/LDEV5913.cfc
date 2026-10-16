component extends="org.lucee.cfml.test.LuceeTestCase"  {

	function run( testResults, testBox ){
		describe( "LDEV-5913 - super method resolution with closures", function(){

			it( "should be able to call super methods from within nested closures", function(){
				// Use _internalRequest to isolate from TestBox's closure context
				local.result = _internalRequest(
					template: createURI( "LDEV5913/ldev5913.cfm" )
				);
				expect( local.result.filecontent.trim() ).toBe( "SUCCESS" );
			} );

		} );
	}

	private string function createURI( string calledName ) {
		var baseURI = "/test/#listLast( getDirectoryFromPath( getCurrenttemplatepath() ), "\/")#/";
		return baseURI & "" & calledName;
	}

}
