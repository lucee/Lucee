component extends="org.lucee.cfml.test.LuceeTestCase" labels="component,property,accessors" {

	function run( testResults, testBox ) {
		describe( "LDEV-6272: cfproperty must not generate accessor UDFs unless accessors=true is set", function() {

			it( "tag-based cfproperty without accessors=true does not create getXxx in this scope", function() {
				var obj = new LDEV6272.NoAccessorsComponent();
				expect( structKeyExists( obj, "getCache" ) ).toBeFalse();
				expect( structKeyExists( obj, "setCache" ) ).toBeFalse();
			});

			it( "script-based property without accessors=true does not create getXxx in this scope", function() {
				var obj = new LDEV6272.ScriptNoAccessors();
				expect( structKeyExists( obj, "getMessage" ) ).toBeFalse();
				expect( structKeyExists( obj, "setMessage" ) ).toBeFalse();
			});

			it( "accessors=true still generates accessors as expected", function() {
				var obj = new LDEV6272.WithAccessors();
				expect( structKeyExists( obj, "getMessage" ) ).toBeTrue();
				expect( structKeyExists( obj, "setMessage" ) ).toBeTrue();
			});

		});
	}

}
