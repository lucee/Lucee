component extends="org.lucee.cfml.test.LuceeTestCase" labels="component,property,accessors" {

	function run( testResults, testBox ) {
		describe( "LDEV-6271: property default vs same-named method with accessors=true", function() {

			it( "getter returns property default before builder method is called", function() {
				var obj = new LDEV6271.BuilderComponent();
				expect( obj.getMyFlag() ).toBeTrue();
			});

			it( "variables scope resolves to property default, not the UDF", function() {
				var obj = new LDEV6271.BuilderComponent();
				expect( obj.readFlagViaVariables() ).toBe( "true" );
			});

			it( "property default can be used in a boolean context", function() {
				var obj = new LDEV6271.BuilderComponent();
				expect( obj.useFlagAsBoolean() ).toBeTrue();
			});

			it( "builder method updates the property value", function() {
				var obj = new LDEV6271.BuilderComponent();
				obj.myFlag( false );
				expect( obj.getMyFlag() ).toBeFalse();
				expect( obj.useFlagAsBoolean() ).toBeFalse();
			});

			it( "property without a same-named method is unaffected", function() {
				var obj = new LDEV6271.BuilderComponent();
				expect( obj.getLabel() ).toBe( "hello" );
				expect( obj.readLabelViaVariables() ).toBe( "hello" );
			});

		});
	}

}
