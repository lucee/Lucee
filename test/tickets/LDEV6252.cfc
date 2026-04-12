component extends="org.lucee.cfml.test.LuceeTestCase" {

	function beforeAll() {
		variables.originalNS = getApplicationSettings().nullSupport;
	}

	function afterAll() {
		application action="update" NULLSupport=variables.originalNS;
	}

	function run( testResults, testBox ) {

		describe( "LDEV-6252: ComponentScopeShadow special key resolution", function() {

			it( "variables.this returns the component public scope", function() {
				var c = new LDEV6252.Child( name: "Zac", age: 42 );
				var vThis = c.getVariablesThis();
				expect( isStruct( vThis ) ).toBeTrue();
				expect( structKeyExists( vThis, "getVariablesThis" ) ).toBeTrue();
			});

			it( "variables.super returns the parent component", function() {
				var c = new LDEV6252.Child( name: "Zac", age: 42 );
				var vSuper = c.getVariablesSuper();
				expect( isObject( vSuper ) ).toBeTrue();
				expect( structKeyExists( vSuper, "baseMethod" ) ).toBeTrue();
			});

			it( "variables.super.baseMethod() invokes the parent", function() {
				var c = new LDEV6252.Child( name: "Zac", age: 42 );
				expect( c.callSuperBaseMethod() ).toBe( "from base" );
			});

			it( "variables.static returns the static scope with inherited values", function() {
				var c = new LDEV6252.Child( name: "Zac", age: 42 );
				var vStatic = c.getVariablesStatic();
				expect( isStruct( vStatic ) ).toBeTrue();
				expect( vStatic.colour ).toBe( "red" );
			});

			it( "normal properties resolve via shadow map", function() {
				var c = new LDEV6252.Child( name: "Zac", age: 42 );
				expect( c.getPropertyViaVariables( "name" ) ).toBe( "Zac" );
				expect( c.getPropertyViaVariables( "age" ) ).toBe( 42 );
			});

			it( "containsKey returns true for THIS, SUPER, STATIC", function() {
				var c = new LDEV6252.Child( name: "Zac", age: 42 );
				expect( c.variablesContainsKey( "this" ) ).toBeTrue();
				expect( c.variablesContainsKey( "super" ) ).toBeTrue();
				expect( c.variablesContainsKey( "static" ) ).toBeTrue();
			});

			it( "containsKey returns true for normal properties", function() {
				var c = new LDEV6252.Child( name: "Zac", age: 42 );
				expect( c.variablesContainsKey( "name" ) ).toBeTrue();
				expect( c.variablesContainsKey( "age" ) ).toBeTrue();
				expect( c.variablesContainsKey( "nonexistent" ) ).toBeFalse();
			});

			it( "setting variables.this does not overwrite the special key", function() {
				var c = new LDEV6252.Child( name: "Zac", age: 42 );
				var vThis = c.trySetVariablesThis();
				// set() guard silently drops the write — should still return the component
				expect( isStruct( vThis ) ).toBeTrue();
				expect( structKeyExists( vThis, "getVariablesThis" ) ).toBeTrue();
			});

			it( "setting variables.super does not overwrite the special key", function() {
				var c = new LDEV6252.Child( name: "Zac", age: 42 );
				var vSuper = c.trySetVariablesSuper();
				expect( isObject( vSuper ) ).toBeTrue();
				expect( structKeyExists( vSuper, "baseMethod" ) ).toBeTrue();
			});

			it( "structKeyList on variables.this includes public members", function() {
				var c = new LDEV6252.Child( name: "Zac", age: 42 );
				var vThis = c.getVariablesThis();
				var keys = structKeyList( vThis );
				expect( listFindNoCase( keys, "getVariablesThis" ) ).toBeGT( 0 );
				expect( listFindNoCase( keys, "childMethod" ) ).toBeGT( 0 );
			});
		});

		describe( "LDEV-6252: null property values through shadow scope", function() {

			it( "null property value — no full null support", function() {
				withNullSupport( false, function() {
					var c = new LDEV6252.Child( name: "Zac", age: 42 );
					c.setPropertyNull( "name" );
					// without FNS, null value means key doesn't "exist"
					expect( c.variablesContainsKey( "name" ) ).toBeFalse();
				});
			});

			it( "null property value — full null support", function() {
				withNullSupport( true, function() {
					var c = new LDEV6252.Child( name: "Zac", age: 42 );
					c.setPropertyNull( "name" );
					// Known FNS limitation: ComponentScopeShadow.containsKey() uses get(key,null)!=null
					// so a null value returns false even with FNS. This differs from StructImpl which
					// uses map.containsKey() directly. Pre-existing behaviour across 6.2, 7.0, 7.1.
					expect( c.variablesContainsKey( "name" ) ).toBeFalse();
					expect( isNull( c.getPropertyViaVariables( "name" ) ) ).toBeTrue();
				});
			});

			it( "non-null property still resolves after null set — no FNS", function() {
				withNullSupport( false, function() {
					var c = new LDEV6252.Child( name: "Zac", age: 42 );
					c.setPropertyNull( "name" );
					// age should still work fine
					expect( c.getPropertyViaVariables( "age" ) ).toBe( 42 );
				});
			});

			it( "special keys still resolve when a property is null — FNS", function() {
				withNullSupport( true, function() {
					var c = new LDEV6252.Child( name: "Zac", age: 42 );
					c.setPropertyNull( "name" );
					// THIS/SUPER/STATIC should still work
					expect( isStruct( c.getVariablesThis() ) ).toBeTrue();
					expect( isObject( c.getVariablesSuper() ) ).toBeTrue();
					expect( c.getVariablesStatic().colour ).toBe( "red" );
				});
			});

		});
	}

	private function withNullSupport( boolean enabled, required function testFn ) {
		application action="update" NULLSupport=arguments.enabled;
		arguments.testFn();
	}

}
