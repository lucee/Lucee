component extends = "org.lucee.cfml.test.LuceeTestCase" labels="java" skip="true" {

	variables.adm = new Administrator('server', request.SERVERADMINPASSWORD?:server.SERVERADMINPASSWORD);

	function beforeAll(){
		adm.updateDefaultSecurityManager(
			direct_java_access: false
		);
	}

	function afterAll(){
		adm.updateDefaultSecurityManager(
			direct_java_access: false
		);
	}

	function run( testResults, testBox ){
		describe( "Test case for LDEV-4853", function(){
			it( title = "Checking direct_java_access disables java UDFs", body = function( currentSpec ){

				int function disabledJavaTest() type="java" {
					return 1;
				}

				expect( function(){
					disabledJavaTest() ;
				}).toThrow();

			});
		});
	}

}