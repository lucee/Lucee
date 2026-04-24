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

		describe( "LDEV-6271: explicit method not shadowed by similarly-named property accessor", function() {

			it( "getCache() returns controller value, not null from cachebox property", function() {
				var obj = new LDEV6271.SupertypeComponent();
				var cache = obj.getCache( "default" );
				expect( cache ).notToBeNull();
				expect( cache.getName() ).toBe( "default" );
			});

			it( "getCachebox() accessor still works independently", function() {
				var obj = new LDEV6271.SupertypeComponent();
				// cachebox property has no default, so getter returns null
				expect( isNull( obj.getCachebox() ) ).toBeTrue();
			});

			it( "getCache() works when called internally without this prefix", function() {
				var obj = new LDEV6271.SupertypeComponent();
				// mirrors ColdBox includeUDF() pattern: internal getCache().getOrSet()
				var result = obj.doInternalWork();
				expect( result ).toBe( "value" );
			});

		});

		describe( "LDEV-6271: inherited accessors with child component (ColdBox Interceptor pattern)", function() {

			it( "child inherits getCache() and it works externally", function() {
				var obj = new LDEV6271.ChildComponent();
				var cache = obj.getCache( "default" );
				expect( cache ).notToBeNull();
				expect( cache.getName() ).toBe( "default" );
			});

			it( "child calls getCache() internally via inherited doInternalWork()", function() {
				var obj = new LDEV6271.ChildComponent();
				var result = obj.doInternalWork();
				expect( result ).toBe( "value" );
			});

		});

		describe( "LDEV-6271: child property 'cache' must not shadow parent's explicit getCache()", function() {

			it( "getCache() calls parent method, not generated accessor for child property", function() {
				var obj = new LDEV6271.ShadowingChild();
				var cache = obj.getCache( "default" );
				expect( cache ).notToBeNull();
				expect( cache.getName() ).toBe( "default" );
			});

			it( "getCache() works internally when child has property named cache", function() {
				var obj = new LDEV6271.ShadowingChild();
				var result = obj.doInternalWork();
				expect( result ).toBe( "value" );
			});

		});

		describe( "LDEV-6271: tag-based child with property 'cache' (3-level inheritance)", function() {

			it( "getCache() calls parent method, not generated accessor", function() {
				var obj = new LDEV6271.TagShadowingChild();
				var cache = obj.getCache( "default" );
				expect( cache ).notToBeNull();
				expect( cache.getName() ).toBe( "default" );
			});

			it( "getCache() works internally via doInternalWork()", function() {
				var obj = new LDEV6271.TagShadowingChild();
				var result = obj.doInternalWork();
				expect( result ).toBe( "value" );
			});

		});

		describe( "LDEV-6271: component without accessors=true must not generate accessor UDFs", function() {

			it( "cfproperty without accessors=true does not create getXxx in this scope", function() {
				var obj = new LDEV6271.NoAccessorsComponent();
				expect( structKeyExists( obj, "getCache" ) ).toBeFalse();
				expect( structKeyExists( obj, "setCache" ) ).toBeFalse();
			});

		});
	}

}
