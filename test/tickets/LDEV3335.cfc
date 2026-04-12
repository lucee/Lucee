component extends="org.lucee.cfml.test.LuceeTestCase" {
	function beforeAll() {
		variables.uri = createURI("LDEV3335");
	}
	function run( testResults, testBox ){
		describe( title="Basic accessor functionality", body=function(){
			it( title="Basic accessors work", body=function( currentSpec ){
				var cfc = new LDEV3335.testWithAccessors();
				cfc.setA("test value");
				expect(cfc.getA()).toBe("test value");
			});
			it( title="Multiple instances are isolated", body=function( currentSpec ){
				var inst1 = new LDEV3335.testWithAccessors();
				var inst2 = new LDEV3335.testWithAccessors();
				var inst3 = new LDEV3335.testWithAccessors();
				inst1.setA("inst1");
				inst2.setA("inst2");
				inst3.setA("inst3");
				expect(inst1.getA()).toBe("inst1");
				expect(inst2.getA()).toBe("inst2");
				expect(inst3.getA()).toBe("inst3");
			});
			it( title="Default property values work", body=function( currentSpec ){
				var cfc = new LDEV3335.testWithAccessors();
				expect(cfc.getA()).toBe("1");
			});
		});

		describe( title="Manual method overrides", body=function(){
			it( title="Manual getter overrides auto-generated accessor", body=function( currentSpec ){
				var cfc = new LDEV3335.testManualGetter();
				expect(cfc.getName()).toBe("UPPERCASE");
			});
			it( title="Manual setter overrides auto-generated accessor", body=function( currentSpec ){
				var cfc = new LDEV3335.testManualSetter();
				cfc.setValue(5);
				expect(cfc.getValue()).toBe(10); // Manual setter doubles the value
			});
		});

		describe( title="Inheritance behavior", body=function(){
			it( title="Child inherits parent property accessors", body=function( currentSpec ){
				var child = new LDEV3335.testInheritChild();
				expect(child.getParentProp()).toBe("from parent");
			});
			it( title="Child manual method overrides parent accessor", body=function( currentSpec ){
				var child = new LDEV3335.testInheritChildOverride();
				expect(child.getParentProp()).toBe("CHILD OVERRIDE");
			});
		});

		describe( title="Property attributes", body=function(){
			it( title="Property types are respected (numeric)", body=function( currentSpec ){
				var cfc = new LDEV3335.testPropertyTypes();
				expect(isNumeric(cfc.getAge())).toBeTrue();
			});
			it( title="Property types are respected (boolean)", body=function( currentSpec ){
				var cfc = new LDEV3335.testPropertyTypes();
				expect(isBoolean(cfc.getActive())).toBeTrue();
			});
			it( title="getter=false / setter=false flags respected", body=function( currentSpec ){
				var cfc = new LDEV3335.testGetterSetterFlags();
				// Should have getter
				expect(cfc.getReadOnly()).toBe("readonly value");
				// Should NOT have setter
				expect(function(){
					cfc.setReadOnly("new");
				}).toThrow();
			});
		});

		describe( title="Dynamic method behavior", body=function(){
			it( title="Dynamic mixin method doesn't interfere with accessors", body=function( currentSpec ){
				var cfc = new LDEV3335.testPropertyTypes();
				// Accessor should work before mixin
				cfc.setAge(25);
				expect(cfc.getAge()).toBe(25);
				// Add a dynamic method (mixin)
				cfc.customMethod = function(){ return "mixin works"; };
				expect(cfc.customMethod()).toBe("mixin works");
				// Accessor should still work after mixin
				cfc.setAge(30);
				expect(cfc.getAge()).toBe(30);
			});
			it( title="Dynamic override of accessor method", body=function( currentSpec ){
				var cfc = new LDEV3335.testPropertyTypes();
				cfc.setAge(25);
				expect(cfc.getAge()).toBe(25);
				// Override the getter with a dynamic method
				cfc.getAge = function(){ return 999; };
				// Should use the dynamic override
				expect(cfc.getAge()).toBe(999);
			});
		});

		describe( title="LDEV-6236 duplicate() with accessor bypass", body=function(){
			it( title="duplicate() preserves accessor functionality", body=function( currentSpec ){
				var orig = new LDEV3335.testPropertyTypes();
				orig.setAge( 25 );
				orig.setName( "original" );
				var copy = duplicate( orig );
				// copy should have its own values
				expect( copy.getAge() ).toBe( 25 );
				expect( copy.getName() ).toBe( "original" );
				// mutations are isolated
				copy.setAge( 99 );
				copy.setName( "copy" );
				expect( orig.getAge() ).toBe( 25 );
				expect( orig.getName() ).toBe( "original" );
				expect( copy.getAge() ).toBe( 99 );
				expect( copy.getName() ).toBe( "copy" );
			});
			it( title="duplicate() preserves typed property casting", body=function( currentSpec ){
				var orig = new LDEV3335.testPropertyTypes();
				var copy = duplicate( orig );
				copy.setAge( "42" );
				expect( copy.getAge() ).toBe( 42 );
				expect( isNumeric( copy.getAge() ) ).toBeTrue();
			});
			it( title="duplicate() with inheritance - child accessors work", body=function( currentSpec ){
				var child = new LDEV3335.testInheritChild();
				var copy = duplicate( child );
				expect( copy.getParentProp() ).toBe( "from parent" );
				copy.setParentProp( "modified" );
				expect( copy.getParentProp() ).toBe( "modified" );
				expect( child.getParentProp() ).toBe( "from parent" );
			});
			it( title="duplicate() with manual override - override preserved", body=function( currentSpec ){
				var orig = new LDEV3335.testManualSetter();
				var copy = duplicate( orig );
				copy.setValue( 5 );
				// manual setter doubles the value
				expect( copy.getValue() ).toBe( 10 );
			});
			it( title="duplicate() with child override - override preserved", body=function( currentSpec ){
				var orig = new LDEV3335.testInheritChildOverride();
				var copy = duplicate( orig );
				expect( copy.getParentProp() ).toBe( "CHILD OVERRIDE" );
			});
			it( title="multiple duplicates are fully isolated", body=function( currentSpec ){
				var orig = new LDEV3335.testWithAccessors();
				orig.setA( "v1" );
				var copy1 = duplicate( orig );
				var copy2 = duplicate( orig );
				copy1.setA( "c1" );
				copy2.setA( "c2" );
				expect( orig.getA() ).toBe( "v1" );
				expect( copy1.getA() ).toBe( "c1" );
				expect( copy2.getA() ).toBe( "c2" );
			});
			it( title="mixed generated and manual accessors on same component", body=function( currentSpec ){
				// testManualGetter has accessors=true with manual getName() but generated setName()
				var orig = new LDEV3335.testManualGetter();
				// generated setter
				orig.setName( "hello" );
				// manual getter (ucases the value)
				expect( orig.getName() ).toBe( "HELLO" );
				// duplicate and verify both paths still work
				var copy = duplicate( orig );
				expect( copy.getName() ).toBe( "HELLO" );
				copy.setName( "world" );
				expect( copy.getName() ).toBe( "WORLD" );
				// original unchanged
				expect( orig.getName() ).toBe( "HELLO" );
			});
			it( title="mixed generated getter and manual setter on same component", body=function( currentSpec ){
				// testManualSetter has accessors=true with manual setValue() but generated getValue()
				var orig = new LDEV3335.testManualSetter();
				// manual setter (doubles the value)
				orig.setValue( 5 );
				// generated getter
				expect( orig.getValue() ).toBe( 10 );
				// duplicate and verify both paths
				var copy = duplicate( orig );
				expect( copy.getValue() ).toBe( 10 );
				copy.setValue( 7 );
				expect( copy.getValue() ).toBe( 14 );
				expect( orig.getValue() ).toBe( 10 );
			});
			it( title="setter chaining works on duplicate", body=function( currentSpec ){
				var orig = new LDEV3335.testPropertyTypes();
				var copy = duplicate( orig );
				var result = copy.setAge( 30 );
				// setter returns the component for chaining
				expect( result.getName() ).toBe( "John" );
			});
		});

		describe( title="Component size tests", body=function(){
			xit( title="Check size of the component with no accessors", body=function( currentSpec ){
				local.result = _InternalRequest(
					template : "#uri#\test.cfm",
					FORM : { scene : 1 }
				);
				expect(trim(result.fileContent)).toBeLT(1000);
			});
			xit( title="Check size of the component with manual setters/getters", body=function( currentSpec ){
				local.result = _InternalRequest(
					template : "#uri#\test.cfm",
					FORM : { scene : 2 }
				);
				expect(trim(result.fileContent)).toBeLT(5000);
			});
			xit( title="Check size of the component with accessors", body=function( currentSpec ){
				local.result = _InternalRequest(
					template : "#uri#\test.cfm",
					FORM : { scene : 3 }
				);
				expect(trim(result.fileContent)).toBeLT(5000);
			});
		});

		describe( title="Static scope inheritance tests", body=function(){
			it( title="Access static method on base component", body=function( currentSpec ){
				var result = LDEV3335.BaseComponent::baseStaticMethod();
				expect(result).toBe("base static method");
			});
			it( title="Access static method on child component", body=function( currentSpec ){
				var result = LDEV3335.ChildComponent::childStaticMethod();
				expect(result).toBe("child static method");
			});
			it( title="Access base static method through child (bcp null issue)", body=function( currentSpec ){
				var result = LDEV3335.ChildComponent::baseStaticMethod();
				expect(result).toBe("base static method");
			});
			it( title="Access static method via variable reference (benchmark pattern)", body=function( currentSpec ){
				local.result = _InternalRequest(
					template : "#uri#\testStaticViaVariable.cfm"
				);
				expect(trim(result.fileContent)).toBe('{"table":true,"name":true}');
			});
		});
	}
	private string function createURI(string calledName){
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}