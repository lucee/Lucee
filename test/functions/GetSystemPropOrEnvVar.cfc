component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults, testBox ) {
		describe( title = "Testcase for GetSystemPropOrEnvVar() function", body = function() {

			it( title = "Checking GetSystemPropOrEnvVar function", body = function( currentSpec ) {
				var props = GetSystemPropOrEnvVar();
				expect(	props ).toBeArray();
				expect(	len(props) ).toBeGT( 0 );
			});

			it( title = "Check sysProp matches envVar", body = function( currentSpec ) {
				var props = GetSystemPropOrEnvVar();
				ArrayEach( props, function( item ){
					var sysPropNameAsEnv = uCase(Replace( item.sysProp, ".", "_", "all" ));
					expect( item.envVar ).toBe( sysPropNameAsEnv );
					expect( item ).toHaveKey( "desc", item.sysProp );
				});
			});

			// first configured value is ever resolved, env before property, but they can differ in values
			xit( title = "Checking GetSystemPropOrEnvVar(prop) function", body = function( currentSpec ) {
				var props = GetSystemPropOrEnvVar();
				ArrayEach( props, function( item ){
					var envVar = GetSystemPropOrEnvVar( item.envVar );
					var sysProp = GetSystemPropOrEnvVar( item.sysProp );
					
					if ( sysProp neq envVar) {
						// manually fail as not to reveal secrets
						fail( "envar [#item.envvar#] which is len() #len(envVar)# neq sysprop [#item.sysprop#] which is len() #len(sysProp)#" );
					}
				});
			});
			
			//For LDEV-5425
			xit( title = "should return the default value instead of an empty value for GetSystemPropOrEnvVar(prop)", body = function( currentSpec ) {
                var props = GetSystemPropOrEnvVar();
                ArrayEach( props, function( item ){
                    var result = GetSystemPropOrEnvVar( item.sysProp );
                    expect( len(result) ).toBeGT( 0 );
                });
            })

		});
	}
}