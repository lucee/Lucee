component extends="org.lucee.cfml.test.LuceeTestCase" labels="component,property,accessors" {

	// LDEV-6271 fix shipped in 7.1 — three tests below assert behaviour the fix introduced.
	// On 7.0 and 6.2 the fix isn't present (latent pre-existing bugs that 7.1 happens to address);
	// per the version compatibility plan, no backport — those cases skip on pre-7.1 runs.
	variables.preLDEV6271 = !server.checkVersionGTE( server.lucee.version, 7, 1 );

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

		describe( "LDEV-6271: inherited property default + child UDF same name (ACF/7.0 parity)", function() {

			it( "child UDF is callable as a method", function() {
				var obj = new LDEV6271.InheritedDefaultChild();
				expect( obj.status() ).toBe( "child-method-result" );
			});

			it( "child UDF stomps the inherited parent default in variables scope (matches ACF/7.0)", function() {
				// LDEV-6271 only protects defaults seeded by THIS component's setProperty.
				// Parent's default lives in shared shadow but child's seededDefaultKeys is empty
				// (child has no own status property), so child's body UDF wins the scope slot.
				var obj = new LDEV6271.InheritedDefaultChild();
				expect( obj.isStatusUdf() ).toBeTrue();
			});

			it( "unrelated parent property default is unaffected", function() {
				var obj = new LDEV6271.InheritedDefaultChild();
				expect( obj.readCounterViaVariables() ).toBe( 42 );
			});

		});

		describe( "LDEV-6271: child redefines parent property + same-named child UDF", function() {

			it( "child's property default takes precedence over parent's in scope", function() {
				var obj = new LDEV6271.OverridingDefaultChild();
				expect( obj.readStatusViaVariables() ).toBe( "child-default" );
			});

			it( "child UDF is still callable", function() {
				var obj = new LDEV6271.OverridingDefaultChild();
				expect( obj.status() ).toBe( "child-method-result" );
			});

		});

		describe( "LDEV-6271: Lucee 7 complex/expression property default + same-named UDF", function() {
			// Lucee 7 evaluates complex defaults (#expr#) at runtime via TagProperty bytecode,
			// which writes directly to variables scope — bypassing setProperty's null guard.
			// This describe block checks whether the LDEV-6271 protection still holds in that path.

			it( "non-null expression default is preserved when same-named UDF is declared", function() {
				var obj = new LDEV6271.ComplexNullDefault();
				expect( obj.readLabelViaVariables() ).toBe( "hello-world" );
			});

			it( "UDF with name matching expression-default property is callable", function() {
				var obj = new LDEV6271.ComplexNullDefault();
				expect( obj.evt() ).toBe( "from-method" );
			});

			it( title="expression default that evaluates to null is preserved (not stomped by UDF)", skip=preLDEV6271, body=function() {
				var obj = new LDEV6271.ComplexNullDefault();
				// if variables.evt was stomped by the UDF, isNull(variables.evt) would be false
				// and readEvtViaVariables would return the UDF (or under no-null-support, "absent-or-null")
				expect( obj.isEvtNull() ).toBeTrue();
			});

			it( title="diagnostic: variables.evt is not the UDF after registerUDF runs", skip=preLDEV6271, body=function() {
				var obj = new LDEV6271.ComplexNullDefault();
				// assertion sharpened to distinguish absent / null-stored / udf-stomped
				expect( obj.diagnoseEvt() ).notToBe( "udf-stomped" );
			});

		});

		describe( "LDEV-6271: pseudoConstructor pre-write + same-named UDF declared after", function() {
			// post-LDEV-6271: pre-existing non-UDF scope value wins; UDF stays callable via _udfs
			// pre-LDEV-6271: the UDF would have stomped the pre-write in scope

			it( "explicit variables.x pre-write is preserved when same-named UDF declared after", function() {
				var obj = new LDEV6271.PseudoConstructorWriter();
				expect( obj.readGreetViaVariables() ).toBe( "hello" );
			});

			it( "UDF is still callable as a method", function() {
				var obj = new LDEV6271.PseudoConstructorWriter();
				expect( obj.greet() ).toBe( "from-method" );
			});

		});
	}

}
