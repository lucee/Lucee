component extends="org.lucee.cfml.test.LuceeTestCase" {

	// Generic CFC accessor contracts:
	//   - accessors="true" generates getX/setX from property declarations
	//   - manual getX/setX functions override the generated ones
	//   - typed properties cast on set
	//   - getter=false / setter=false suppress generation
	//   - duplicate() preserves accessor functionality, isolation, casting, mixed manual/auto
	// LDEV-6236's UDFGSProperty share-don't-clone optimization must keep all of this intact.

	function run( testResults, testBox ){
		describe( "CFC accessors", function(){

			describe( "basic accessor functionality", function(){
				it( title="basic accessors work", body=function( currentSpec ){
					var cfc = new accessors.testWithAccessors();
					cfc.setA("test value");
					expect(cfc.getA()).toBe("test value");
				});
				it( title="multiple instances are isolated", body=function( currentSpec ){
					var inst1 = new accessors.testWithAccessors();
					var inst2 = new accessors.testWithAccessors();
					var inst3 = new accessors.testWithAccessors();
					inst1.setA("inst1");
					inst2.setA("inst2");
					inst3.setA("inst3");
					expect(inst1.getA()).toBe("inst1");
					expect(inst2.getA()).toBe("inst2");
					expect(inst3.getA()).toBe("inst3");
				});
				it( title="default property values work", body=function( currentSpec ){
					var cfc = new accessors.testWithAccessors();
					expect(cfc.getA()).toBe("1");
				});
				it( title="50 fresh instances — property values fully isolated", body=function( currentSpec ){
					var instances = [];
					for ( var i=1; i<=50; i++ ) {
						var inst = new accessors.testPropertyTypes();
						inst.setAge( i );
						inst.setName( "inst-" & i );
						arrayAppend( instances, inst );
					}
					for ( var i=1; i<=50; i++ ) {
						expect( instances[ i ].getAge() ).toBe( i );
						expect( instances[ i ].getName() ).toBe( "inst-" & i );
					}
				});
				it( title="callDirect bypass: getter returns the underlying value, not the UDF", body=function( currentSpec ){
					var cfc = new accessors.testPropertyTypes();
					cfc.setAge( 42 );
					var v = cfc.getAge();
					expect( v ).toBe( 42 );
					expect( isCustomFunction( v ) ).toBeFalse();
				});
				it( title="callDirect bypass: setter returns the component for chaining", body=function( currentSpec ){
					var cfc = new accessors.testPropertyTypes();
					var ret = cfc.setAge( 30 );
					expect( ret.getAge() ).toBe( 30 );
				});
			});

			describe( "manual method overrides", function(){
				it( title="manual getter overrides auto-generated accessor", body=function( currentSpec ){
					var cfc = new accessors.testManualGetter();
					expect(cfc.getName()).toBe("UPPERCASE");
				});
				it( title="manual setter overrides auto-generated accessor", body=function( currentSpec ){
					var cfc = new accessors.testManualSetter();
					cfc.setValue(5);
					expect(cfc.getValue()).toBe(10); // manual setter doubles
				});
			});

			describe( "property attributes", function(){
				it( title="property types are respected (numeric)", body=function( currentSpec ){
					var cfc = new accessors.testPropertyTypes();
					expect(isNumeric(cfc.getAge())).toBeTrue();
				});
				it( title="property types are respected (boolean)", body=function( currentSpec ){
					var cfc = new accessors.testPropertyTypes();
					expect(isBoolean(cfc.getActive())).toBeTrue();
				});
				it( title="setter=false: getter works, setter throws", body=function( currentSpec ){
					var cfc = new accessors.testGetterSetterFlags();
					expect( cfc.getReadOnly() ).toBe( "readonly value" );
					expect( function(){ cfc.setReadOnly( "new" ); } ).toThrow();
				});
				it( title="getter=false: setter works, getter throws", body=function( currentSpec ){
					var cfc = new accessors.testGetterSetterFlags();
					expect( function(){ cfc.getWriteOnly(); } ).toThrow();
					cfc.setWriteOnly( "new value" );
					// even after setter is called, getter still doesn't exist
					expect( function(){ cfc.getWriteOnly(); } ).toThrow();
				});
				it( title="setter=false honours the default value", body=function( currentSpec ){
					var cfc = new accessors.testGetterSetterFlags();
					expect( cfc.getReadOnly() ).toBe( "readonly value" );
				});
				it( title="type='any' setter accepts anything", body=function( currentSpec ){
					var cfc = new accessors.testNoDefaults();
					cfc.setB( 42 );
					expect( cfc.getB() ).toBe( 42 );
					cfc.setB( "string" );
					expect( cfc.getB() ).toBe( "string" );
					cfc.setB( [ 1, 2, 3 ] );
					expect( cfc.getB() ).toBe( [ 1, 2, 3 ] );
					cfc.setB( { k: "v" } );
					expect( cfc.getB().k ).toBe( "v" );
				});
				it( title="property without declared type accepts anything", body=function( currentSpec ){
					var cfc = new accessors.testNoDefaults();
					cfc.setA( 42 );
					expect( cfc.getA() ).toBe( 42 );
					cfc.setA( "string" );
					expect( cfc.getA() ).toBe( "string" );
				});
				it( title="getX returns null for property with no default that was never set", body=function( currentSpec ){
					var cfc = new accessors.testNoDefaults();
					expect( isNull( cfc.getA() ) ).toBeTrue();
					expect( isNull( cfc.getB() ) ).toBeTrue();
				});
				it( title="boolean setter rejects non-castable string", body=function( currentSpec ){
					var cfc = new accessors.testPropertyTypes();
					expect( function(){ cfc.setActive( "not-a-bool" ); } ).toThrow();
				});
				it( title="boolean setter accepts CFML-truthy strings (yes/no/true/false/1/0)", body=function( currentSpec ){
					var cfc = new accessors.testPropertyTypes();
					cfc.setActive( "yes" );
					expect( cfc.getActive() ).toBeTrue();
					cfc.setActive( "no" );
					expect( cfc.getActive() ).toBeFalse();
					cfc.setActive( 1 );
					expect( cfc.getActive() ).toBeTrue();
					cfc.setActive( 0 );
					expect( cfc.getActive() ).toBeFalse();
				});
				it( title="typed casting still applies through callDirect path", body=function( currentSpec ){
					var cfc = new accessors.testPropertyTypes();
					cfc.setAge( "42" );
					expect( cfc.getAge() ).toBe( 42 );
					expect( isNumeric( cfc.getAge() ) ).toBeTrue();
				});
				it( title="setter validates castability but does not coerce — string stays a string", body=function( currentSpec ){
					// per the component-accessors recipe: setAge("42") accepts because "42" is castable
					// to numeric, but the value lands in variables as java.lang.String, not a Number.
					// CFML's loose equality and isNumeric() both pass for the string "42", which is
					// why the existing "typed casting still applies" test happens to pass too.
					var cfc = new accessors.testPropertyTypes();
					cfc.setAge( "42" );
					expect( cfc.getAge().getClass().getName() ).toBe( "java.lang.String" );
				});
				it( title="setter rejects values that are not castable to the declared type", body=function( currentSpec ){
					var cfc = new accessors.testPropertyTypes();
					expect( function(){ cfc.setAge( "not-a-number" ); } ).toThrow();
				});
				it( title="failed setter does not corrupt prior state", body=function( currentSpec ){
					var cfc = new accessors.testPropertyTypes();
					cfc.setAge( 25 );
					expect( cfc.getAge() ).toBe( 25 );
					expect( function(){ cfc.setAge( "not-a-number" ); } ).toThrow();
					expect( cfc.getAge() ).toBe( 25 );
				});
				it( title="boolean setter validates castability but does not coerce — string stays a string", body=function( currentSpec ){
					var cfc = new accessors.testPropertyTypes();
					cfc.setActive( "yes" );
					expect( cfc.getActive().getClass().getName() ).toBe( "java.lang.String" );
				});
				it( title="setter called with no args throws", body=function( currentSpec ){
					var cfc = new accessors.testPropertyTypes();
					expect( function(){ cfc.setAge(); } ).toThrow();
				});
				it( title="getter called with extra args ignores them (Lucee tolerates)", body=function( currentSpec ){
					var cfc = new accessors.testPropertyTypes();
					cfc.setAge( 25 );
					expect( cfc.getAge( "ignored" ) ).toBe( 25 );
				});
				it( title="date setter accepts ISO string (castability passes, raw string stored)", body=function( currentSpec ){
					var cfc = new accessors.testDateProperty();
					cfc.setCreated( "2026-05-02" );
					expect( cfc.getCreated() ).toBe( "2026-05-02" );
				});
				it( title="date setter accepts a real Date object", body=function( currentSpec ){
					var cfc = new accessors.testDateProperty();
					var d = now();
					cfc.setCreated( d );
					expect( isDate( cfc.getCreated() ) ).toBeTrue();
				});
				it( title="date setter rejects non-castable string", body=function( currentSpec ){
					var cfc = new accessors.testDateProperty();
					expect( function(){ cfc.setCreated( "not-a-date" ); } ).toThrow();
				});
				it( title="custom CFC type setter accepts the declared type and returns the SAME reference", body=function( currentSpec ){
					var cfc = new accessors.testCustomCfcType();
					var rel = new accessors.testWithAccessors();
					cfc.setRelated( rel );
					// mutate-after-set: if getRelated returns the SAME reference, the mutation is visible.
					// If it returns a clone/wrapper, the cloned copy still has the pre-mutation value.
					rel.setA( "mutated-after-setRelated" );
					expect( cfc.getRelated().getA() ).toBe( "mutated-after-setRelated" );
				});
				it( title="custom CFC type setter rejects a different CFC type", body=function( currentSpec ){
					var cfc = new accessors.testCustomCfcType();
					var different = new accessors.testPropertyTypes();
					expect( function(){ cfc.setRelated( different ); } ).toThrow();
				});
				it( title="custom CFC type setter rejects a struct (not a Component)", body=function( currentSpec ){
					var cfc = new accessors.testCustomCfcType();
					expect( function(){ cfc.setRelated( { id: 1 } ); } ).toThrow();
				});
			});

			describe( "access modifiers — public / private / package / remote", function(){
				it( title="access='public' (default) is callable externally", body=function( currentSpec ){
					var cfc = new accessors.testAccessModifiers();
					expect( cfc.getPub() ).toBe( "public-value" );
					cfc.setPub( "mutated" );
					expect( cfc.getPub() ).toBe( "mutated" );
				});
				it( title="QUIRK: access='private' on property is NOT enforced — accessor externally callable", body=function( currentSpec ){
					// Shared bug — both Lucee and ACF ignore access on auto-accessors; hand-written
					// access='private' methods ARE correctly denied externally on both. Root cause
					// in UDFGSProperty constructor hardcoding super(Component.ACCESS_PUBLIC),
					// dropping the access value that's correctly stored on the PropertyImpl.
					var cfc = new accessors.testAccessModifiers();
					expect( cfc.getPriv() ).toBe( "private-value" );
					cfc.setPriv( "externally-mutated" );
					expect( cfc.getPriv() ).toBe( "externally-mutated" );
				});
				it( title="access='private' accessor still works internally via a public method", body=function( currentSpec ){
					var cfc = new accessors.testAccessModifiers();
					expect( cfc.callPrivateInternally() ).toBe( "private-value" );
					cfc.setPrivateInternally( "internal-mutation" );
					expect( cfc.callPrivateInternally() ).toBe( "internal-mutation" );
				});
				it( title="QUIRK: access='package' on property is NOT enforced — accessor externally callable", body=function( currentSpec ){
					// same shared bug as access='private' — package gate dropped on auto-accessor wiring
					var cfc = new accessors.testAccessModifiers();
					expect( cfc.getPkg() ).toBe( "package-value" );
				});
				it( title="access='remote' is callable externally", body=function( currentSpec ){
					var cfc = new accessors.testAccessModifiers();
					expect( cfc.getRem() ).toBe( "remote-value" );
					cfc.setRem( "mutated-remote" );
					expect( cfc.getRem() ).toBe( "mutated-remote" );
				});
			});

			describe( "inheritance — child inherits parent's auto-generated accessors", function(){
				it( title="child can call inherited getter for parent property", body=function( currentSpec ){
					var cfc = new accessors.testChildAccessor();
					expect( cfc.getParentProp() ).toBe( "from-parent" );
				});
				it( title="child can call inherited setter for parent property", body=function( currentSpec ){
					var cfc = new accessors.testChildAccessor();
					cfc.setParentProp( "mutated-by-child" );
					expect( cfc.getParentProp() ).toBe( "mutated-by-child" );
				});
				it( title="child has its own auto-generated accessors alongside inherited ones", body=function( currentSpec ){
					var cfc = new accessors.testChildAccessor();
					expect( cfc.getChildProp() ).toBe( "from-child" );
					cfc.setChildProp( "child-mutated" );
					expect( cfc.getChildProp() ).toBe( "child-mutated" );
				});
				it( title="parent and child instances have isolated parent-property scopes", body=function( currentSpec ){
					var p = new accessors.testParentAccessor();
					var c = new accessors.testChildAccessor();
					p.setParentProp( "in-parent-only" );
					expect( p.getParentProp() ).toBe( "in-parent-only" );
					expect( c.getParentProp() ).toBe( "from-parent" );
				});
				it( title="grandchild inherits accessors from all levels of the chain", body=function( currentSpec ){
					var cfc = new accessors.testGrandchild();
					expect( cfc.getParentProp() ).toBe( "from-parent" );           // from grandparent
					expect( cfc.getChildProp() ).toBe( "from-child" );             // from parent
					expect( cfc.getGrandchildProp() ).toBe( "from-grandchild" );   // own
				});
				it( title="grandchild can mutate inherited properties at every level", body=function( currentSpec ){
					var cfc = new accessors.testGrandchild();
					cfc.setParentProp( "mutated-grandparent" );
					cfc.setChildProp( "mutated-parent" );
					cfc.setGrandchildProp( "mutated-own" );
					expect( cfc.getParentProp() ).toBe( "mutated-grandparent" );
					expect( cfc.getChildProp() ).toBe( "mutated-parent" );
					expect( cfc.getGrandchildProp() ).toBe( "mutated-own" );
				});
				it( title="child redeclaring parent property with new default — child default wins", body=function( currentSpec ){
					var cfc = new accessors.testChildOverridingDefault();
					expect( cfc.getParentProp() ).toBe( "overridden-by-child" );
				});
				it( title="overriding child instance and plain parent instance have isolated values", body=function( currentSpec ){
					var p = new accessors.testParentAccessor();
					var c = new accessors.testChildOverridingDefault();
					expect( p.getParentProp() ).toBe( "from-parent" );
					expect( c.getParentProp() ).toBe( "overridden-by-child" );
				});
			});

			describe( "no accessors=true — properties without auto-generated getX/setX", function(){
				it( title="component without accessors=true does NOT generate getX/setX", body=function( currentSpec ){
					var cfc = new accessors.testNoAccessors();
					expect( function(){ cfc.getX(); } ).toThrow();
					expect( function(){ cfc.setX( "v" ); } ).toThrow();
				});
			});

			describe( "metadata reflects declared properties", function(){
				it( title="getMetaData(cfc).properties lists declared properties with their attributes", body=function( currentSpec ){
					var cfc = new accessors.testPropertyTypes();
					var meta = getMetaData( cfc );
					expect( meta ).toHaveKey( "properties" );
					var byName = {};
					for ( var p in meta.properties ) byName[ p.name ] = p;
					expect( byName ).toHaveKey( "age" );
					expect( byName.age.type ).toBe( "numeric" );
					expect( byName.age.default ).toBe( "25" );
					expect( byName ).toHaveKey( "active" );
					expect( byName.active.type ).toBe( "boolean" );
					expect( byName ).toHaveKey( "name" );
					expect( byName.name.type ).toBe( "string" );
				});
			});

			describe( "direct ref invocation", function(){
				// per the component-accessors recipe: when an accessor is grabbed as a ref and called
				// directly (not assigned onto another CFC), it stays bound to the source CFC and reads
				// the source's CURRENT variables scope on each call.
				it( title="ref captures the live binding — reads source's current state", body=function( currentSpec ){
					var foo = new accessors.testWithAccessors();
					foo.setA( "first" );
					var ref = foo.getA;
					expect( ref() ).toBe( "first" );
					foo.setA( "second" );
					expect( ref() ).toBe( "second" );
				});
				it( title="ref captured from foo does not read from sibling bar", body=function( currentSpec ){
					var foo = new accessors.testWithAccessors();
					var bar = new accessors.testWithAccessors();
					foo.setA( "from-foo" );
					bar.setA( "from-bar" );
					var fooRef = foo.getA;
					expect( fooRef() ).toBe( "from-foo" );
				});
				it( title="ref assigned onto another CFC rebinds to host (LDEV-1962 contract)", body=function( currentSpec ){
					var foo = new accessors.testWithAccessors();
					var bar = new accessors.testWithAccessors();
					foo.setA( "from-foo" );
					bar.setA( "from-bar" );
					bar.injected = foo.getA;
					expect( bar.injected() ).toBe( "from-bar" );
				});
				it( title="arrayMap with closure calling getX extracts from each instance", body=function( currentSpec ){
					var a = new accessors.testWithAccessors();
					var b = new accessors.testWithAccessors();
					a.setA( "alpha" );
					b.setA( "bravo" );
					var result = arrayMap( [ a, b ], function( cfc ){ return cfc.getA(); });
					expect( result ).toBe( [ "alpha", "bravo" ] );
				});
				it( title="arrayMap collecting refs — each ref stays bound to its source", body=function( currentSpec ){
					var a = new accessors.testWithAccessors();
					var b = new accessors.testWithAccessors();
					a.setA( "alpha" );
					b.setA( "bravo" );
					var refs = arrayMap( [ a, b ], function( cfc ){ return cfc.getA; });
					var fn1 = refs[ 1 ];
					var fn2 = refs[ 2 ];
					expect( fn1() ).toBe( "alpha" );
					expect( fn2() ).toBe( "bravo" );
				});
				it( title="duplicate() of a captured ref preserves the binding to its source component", body=function( currentSpec ){
					var foo = new accessors.testWithAccessors();
					foo.setA( "from-foo" );
					var ref = foo.getA;
					var refDup = duplicate( ref );
					expect( refDup() ).toBe( "from-foo" );
					// mutate source after duplicate — duplicate still reads from foo's live scope
					foo.setA( "mutated-after-dup" );
					expect( refDup() ).toBe( "mutated-after-dup" );
				});
				xit( title="re-extracting an injected accessor rebinds again — confirms unwrap on assignment", body=function( currentSpec ){
					// SKIPPED — known-failing on baseline. The slow-path extraction on 7.0/baseline-7.1
					// returns the raw UDFGetterProperty with srcComponent fallback (reads from foo);
					// the rebind contract only kicks in via the BoundUDF wrap on extraction. Once
					// BoundUDF lands in trunk, flip back to it() and this asserts the fast/slow
					// dispatch paths agree on receiver = host (bar).
					var foo = new accessors.testWithAccessors();
					var bar = new accessors.testWithAccessors();
					foo.setA( "from-foo" );
					bar.setA( "from-bar" );
					bar.injected = foo.getA;
					var ref = bar.injected;
					expect( ref() ).toBe( "from-bar" );
				});
			});

			describe( "storage scope — variables vs this", function(){
				// per the component-accessors recipe: defaults seed variables only; accessors read/write
				// variables; this is a separate (public) scope and is not populated automatically.
				it( title="property default seeds variables but not this", body=function( currentSpec ){
					var cfc = new accessors.testWithAccessors();
					expect( cfc.getA() ).toBe( "1" );
					expect( structKeyExists( cfc, "a" ) ).toBeFalse();
				});
				it( title="setX writes variables, not this", body=function( currentSpec ){
					var cfc = new accessors.testWithAccessors();
					cfc.setA( "x" );
					expect( cfc.getA() ).toBe( "x" );
					expect( structKeyExists( cfc, "a" ) ).toBeFalse();
				});
				it( title="structKeyArray exposes methods (in this scope), not properties (in variables)", body=function( currentSpec ){
					var cfc = new accessors.testWithAccessors();
					var keys = structKeyArray( cfc );
					// methods like getA, setA, init are in this
					expect( arrayFindNoCase( keys, "getA" ) ).toBeGT( 0 );
					expect( arrayFindNoCase( keys, "setA" ) ).toBeGT( 0 );
					// property "a" itself is in variables, not this — should NOT appear
					expect( arrayFindNoCase( keys, "a" ) ).toBe( 0 );
				});
			});

			describe( "expression-default properties (Lucee 7 runtime evaluation)", function(){
				// Lucee 7 evaluates `default="#expression()#"` at runtime via TagProperty bytecode.
				// LDEV-6271 covers the same-name-method shadowing case; these specs lock the
				// non-shadowing baseline.
				it( title="expression default is evaluated at component instantiation", body=function( currentSpec ){
					var cfc = new accessors.testExpressionDefault();
					expect( isDate( cfc.getCreated() ) ).toBeTrue();
					expect( isNumeric( cfc.getRandomNum() ) ).toBeTrue();
				});
				it( title="expression default is evaluated PER INSTANCE — different instances may have different values", body=function( currentSpec ){
					// randRange(1,1M) collision odds are 1 in a million; effectively zero
					var a = new accessors.testExpressionDefault();
					var b = new accessors.testExpressionDefault();
					expect( a.getRandomNum() ).notToBe( b.getRandomNum() );
				});
			});

			describe( "tag-based component (cfcomponent + cfproperty)", function(){
				it( title="tag-based syntax generates accessors same as script syntax", body=function( currentSpec ){
					var cfc = new accessors.testTagBased();
					expect( cfc.getX() ).toBe( "from-tag" );
					cfc.setX( "mutated" );
					expect( cfc.getX() ).toBe( "mutated" );
				});
			});

			describe( "getters return references, not copies", function(){
				// per the component-accessors recipe: for complex types, getX hands back the live
				// reference — mutating it mutates the underlying property.
				it( title="mutating the array from getX changes the underlying property", body=function( currentSpec ){
					var cfc = new accessors.testCollectionArray();
					cfc.setLabels( [ "a", "b" ] );
					var ref = cfc.getLabels();
					arrayAppend( ref, "c" );
					expect( cfc.getLabels() ).toBe( [ "a", "b", "c" ] );
				});
				it( title="mutating the struct from getX changes the underlying property", body=function( currentSpec ){
					var cfc = new accessors.testCollectionStruct();
					cfc.setConfig( { count: 1 } );
					var ref = cfc.getConfig();
					ref.count = 99;
					expect( cfc.getConfig().count ).toBe( 99 );
				});
			});

			// Collection helpers (addX/hasX/removeX) are generated by PropertyFactory based on fieldType,
			// not by accessors=true alone. We exercise the plumbing here without loading Hibernate —
			// full ORM-side coverage lives in the hibernate extension (tests/mapping/singularName, etc.).
			describe( "collection helpers — array property", function(){
				it( title="addX appends to array property", body=function( currentSpec ){
					var cfc = new accessors.testCollectionArray();
					cfc.addTags( "cfml" );
					cfc.addTags( "lucee" );
					var tags = cfc.getTags();
					expect( arrayLen( tags ) ).toBe( 2 );
					expect( tags[ 1 ] ).toBe( "cfml" );
					expect( tags[ 2 ] ).toBe( "lucee" );
				});
				it( title="hasX matches array property by value", body=function( currentSpec ){
					var cfc = new accessors.testCollectionArray();
					cfc.addTags( "cfml" );
					expect( cfc.hasTags( "cfml" ) ).toBeTrue();
					expect( cfc.hasTags( "missing" ) ).toBeFalse();
				});
				it( title="removeX removes from array property by value", body=function( currentSpec ){
					var cfc = new accessors.testCollectionArray();
					cfc.addTags( "cfml" );
					cfc.addTags( "lucee" );
					cfc.removeTags( "cfml" );
					expect( cfc.hasTags( "cfml" ) ).toBeFalse();
					expect( cfc.hasTags( "lucee" ) ).toBeTrue();
				});
				it( title="addX on a fresh component creates the array lazily", body=function( currentSpec ){
					var cfc = new accessors.testCollectionArray();
					cfc.addTags( "first" );
					var tags = cfc.getTags();
					expect( isArray( tags ) ).toBeTrue();
					expect( arrayLen( tags ) ).toBe( 1 );
				});
				it( title="addX appends to a pre-populated array (does not replace)", body=function( currentSpec ){
					var cfc = new accessors.testCollectionArray();
					cfc.setTags( [ "existing" ] );
					cfc.addTags( "added" );
					expect( cfc.getTags() ).toBe( [ "existing", "added" ] );
				});
				it( title="siblings have isolated array collections", body=function( currentSpec ){
					var a = new accessors.testCollectionArray();
					var b = new accessors.testCollectionArray();
					a.addTags( "only-a" );
					expect( a.hasTags( "only-a" ) ).toBeTrue();
					expect( b.hasTags( "only-a" ) ).toBeFalse();
				});
				it( title="bare array property without fieldType gets no collection helpers", body=function( currentSpec ){
					// helpers are gated on fieldType=one-to-many|many-to-many in PropertyFactory.createPropertyUDFs;
					// type=array alone is not enough
					var cfc = new accessors.testCollectionArray();
					expect( function(){ cfc.addLabels( "x" ); } ).toThrow();
					expect( function(){ cfc.hasLabels( "x" ); } ).toThrow();
					expect( function(){ cfc.removeLabels( "x" ); } ).toThrow();
					// getLabels/setLabels still work — those come from accessors=true alone
					cfc.setLabels( [ "a", "b" ] );
					expect( cfc.getLabels() ).toBe( [ "a", "b" ] );
				});
				it( title="fieldType=many-to-many generates addX/hasX/removeX (parity with one-to-many)", body=function( currentSpec ){
					var cfc = new accessors.testCollectionMany2Many();
					cfc.addBookmark( "url1" );
					cfc.addBookmark( "url2" );
					expect( cfc.hasBookmark( "url1" ) ).toBeTrue();
					expect( cfc.hasBookmark( "url2" ) ).toBeTrue();
					cfc.removeBookmark( "url1" );
					expect( cfc.hasBookmark( "url1" ) ).toBeFalse();
					expect( cfc.hasBookmark( "url2" ) ).toBeTrue();
				});
			});

			describe( "fieldType=one-to-one / many-to-one — only hasX is generated", function(){
				// per PropertyFactory.createPropertyUDFs: these relationship types get hasX as a
				// "is the relationship populated?" check, but no addX/removeX (single entity, not a collection).
				it( title="one-to-one generates hasX only (no addX/removeX)", body=function( currentSpec ){
					var cfc = new accessors.testRelationshipOne2One();
					expect( cfc.hasPassport() ).toBeFalse();
					expect( function(){ cfc.addPassport( "x" ); } ).toThrow();
					expect( function(){ cfc.removePassport( "x" ); } ).toThrow();
				});
				it( title="QUIRK: one-to-one hasX returns true only when set to a Component (ACF parity gap)", body=function( currentSpec ){
					// Parity gap — ACF uses `value != null` (with empty-collection carve-outs);
					// Lucee uses strict `instanceof Component` at UDFHasProperty.has() line 117.
					// Lazy proxies (CFCHibernateProxy) satisfy instanceof Component, so loosening
					// to ACF parity wouldn't break the loaded-entity path. One-line fix.
					var cfc = new accessors.testRelationshipOne2One();
					expect( cfc.hasPassport() ).toBeFalse();
					cfc.setPassport( { id: 1 } );
					expect( cfc.hasPassport() ).toBeFalse();
					cfc.setPassport( new accessors.testWithAccessors() );
					expect( cfc.hasPassport() ).toBeTrue();
				});
				it( title="many-to-one generates hasX only (no addX/removeX)", body=function( currentSpec ){
					var cfc = new accessors.testRelationshipMany2One();
					expect( cfc.hasDepartment() ).toBeFalse();
					expect( function(){ cfc.addDepartment( "x" ); } ).toThrow();
					expect( function(){ cfc.removeDepartment( "x" ); } ).toThrow();
				});
				it( title="QUIRK: many-to-one hasX returns true only when set to a Component (same parity gap)", body=function( currentSpec ){
					var cfc = new accessors.testRelationshipMany2One();
					cfc.setDepartment( { id: 1, name: "engineering" } );
					expect( cfc.hasDepartment() ).toBeFalse();
					cfc.setDepartment( new accessors.testWithAccessors() );
					expect( cfc.hasDepartment() ).toBeTrue();
				});
			});

			describe( "persistent=true implies accessors=true (recipe claim)", function(){
				// recipe states: "Persistent (ORM) components get them automatically —
				// accessors='true' is implicit there"
				it( title="persistent=true component generates getX/setX without explicit accessors=true", body=function( currentSpec ){
					var cfc = new accessors.testPersistentImplicitAccessors();
					expect( cfc.getX() ).toBe( "from-default" );
					cfc.setX( "mutated" );
					expect( cfc.getX() ).toBe( "mutated" );
				});
			});

			describe( "collection helpers — struct property", function(){
				xit( title="addX sets struct property by key (dot notation)", body=function( currentSpec ){
					// SKIPPED — shared bug with ACF: UDFAddProperty._call lazy-inits java.util.HashMap
					// (case-sensitive), so CFML's uppercased dot notation throws. Loaded-state path
					// (Hibernate's PersistentMap) has the same case-sensitivity. Lazy-init fix is
					// new StructImpl() in core; loaded-state needs a Lucee-aware Map wrapper in
					// the hibernate extension's CFCSetter/CFCGetter.
					var cfc = new accessors.testCollectionStruct();
					cfc.addMeta( "author", "zac" );
					expect( cfc.getMeta().author ).toBe( "zac" );
				});
				it( title="addX sets struct property by key (helper-based access)", body=function( currentSpec ){
					// helper accessors bypass the case folding by going through Map.containsKey directly
					var cfc = new accessors.testCollectionStruct();
					cfc.addMeta( "author", "zac" );
					expect( cfc.hasMeta( "author" ) ).toBeTrue();
				});
				it( title="hasX matches struct property by key", body=function( currentSpec ){
					var cfc = new accessors.testCollectionStruct();
					cfc.addMeta( "author", "zac" );
					expect( cfc.hasMeta( "author" ) ).toBeTrue();
					expect( cfc.hasMeta( "missing" ) ).toBeFalse();
				});
				it( title="removeX removes from struct property by key", body=function( currentSpec ){
					var cfc = new accessors.testCollectionStruct();
					cfc.addMeta( "author", "zac" );
					cfc.addMeta( "year", 2026 );
					cfc.removeMeta( "author" );
					expect( cfc.hasMeta( "author" ) ).toBeFalse();
					expect( cfc.hasMeta( "year" ) ).toBeTrue();
				});
				it( title="addX on a fresh component creates the struct lazily", body=function( currentSpec ){
					var cfc = new accessors.testCollectionStruct();
					cfc.addMeta( "k", "v" );
					var meta = cfc.getMeta();
					expect( isStruct( meta ) ).toBeTrue();
					expect( structCount( meta ) ).toBe( 1 );
				});
				it( title="addX merges into a pre-populated struct (Lucee Struct path)", body=function( currentSpec ){
					// when the property is already a Lucee Struct (via setX), addX hits the
					// `instanceof Struct` branch and dot notation works fine — confirms the
					// HashMap bug is purely in the lazy-init path, not in addX itself
					var cfc = new accessors.testCollectionStruct();
					cfc.setMeta( { existing: "value" } );
					cfc.addMeta( "added", "newvalue" );
					expect( cfc.hasMeta( "existing" ) ).toBeTrue();
					expect( cfc.hasMeta( "added" ) ).toBeTrue();
					expect( cfc.getMeta().existing ).toBe( "value" );
					expect( cfc.getMeta().added ).toBe( "newvalue" );
				});
				it( title="siblings have isolated struct collections", body=function( currentSpec ){
					var a = new accessors.testCollectionStruct();
					var b = new accessors.testCollectionStruct();
					a.addMeta( "only", "in-a" );
					expect( a.hasMeta( "only" ) ).toBeTrue();
					expect( b.hasMeta( "only" ) ).toBeFalse();
				});
				it( title="bare struct property without fieldType gets no collection helpers", body=function( currentSpec ){
					var cfc = new accessors.testCollectionStruct();
					expect( function(){ cfc.addConfig( "k", "v" ); } ).toThrow();
					expect( function(){ cfc.hasConfig( "k" ); } ).toThrow();
					expect( function(){ cfc.removeConfig( "k" ); } ).toThrow();
					// getConfig/setConfig still work — those come from accessors=true alone
					cfc.setConfig( { k: "v" } );
					expect( cfc.getConfig().k ).toBe( "v" );
				});
			});

			describe( "singularName overrides collection helper names", function(){
				it( title="addX/hasX/removeX use singularName when declared", body=function( currentSpec ){
					var cfc = new accessors.testSingularName();
					cfc.addTag( "cfml" );
					expect( cfc.hasTag( "cfml" ) ).toBeTrue();
					cfc.removeTag( "cfml" );
					expect( cfc.hasTag( "cfml" ) ).toBeFalse();
				});
				it( title="getX still uses the property name verbatim", body=function( currentSpec ){
					var cfc = new accessors.testSingularName();
					cfc.addTag( "cfml" );
					var tags = cfc.getTags();
					expect( arrayLen( tags ) ).toBe( 1 );
					expect( tags[ 1 ] ).toBe( "cfml" );
				});
			});

			describe( "duplicate() preserves accessor behaviour", function(){
				it( title="duplicate() preserves accessor functionality", body=function( currentSpec ){
					var orig = new accessors.testPropertyTypes();
					orig.setAge( 25 );
					orig.setName( "original" );
					var copy = duplicate( orig );
					expect( copy.getAge() ).toBe( 25 );
					expect( copy.getName() ).toBe( "original" );
					copy.setAge( 99 );
					copy.setName( "copy" );
					expect( orig.getAge() ).toBe( 25 );
					expect( orig.getName() ).toBe( "original" );
					expect( copy.getAge() ).toBe( 99 );
					expect( copy.getName() ).toBe( "copy" );
				});
				it( title="duplicate() preserves typed property casting", body=function( currentSpec ){
					var orig = new accessors.testPropertyTypes();
					var copy = duplicate( orig );
					copy.setAge( "42" );
					expect( copy.getAge() ).toBe( 42 );
					expect( isNumeric( copy.getAge() ) ).toBeTrue();
				});
				it( title="duplicate() with manual override - override preserved", body=function( currentSpec ){
					var orig = new accessors.testManualSetter();
					var copy = duplicate( orig );
					copy.setValue( 5 );
					expect( copy.getValue() ).toBe( 10 );
				});
				it( title="multiple duplicates are fully isolated", body=function( currentSpec ){
					var orig = new accessors.testWithAccessors();
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
					var orig = new accessors.testManualGetter();
					orig.setName( "hello" );
					expect( orig.getName() ).toBe( "HELLO" );
					var copy = duplicate( orig );
					expect( copy.getName() ).toBe( "HELLO" );
					copy.setName( "world" );
					expect( copy.getName() ).toBe( "WORLD" );
					expect( orig.getName() ).toBe( "HELLO" );
				});
				it( title="mixed generated getter and manual setter on same component", body=function( currentSpec ){
					var orig = new accessors.testManualSetter();
					orig.setValue( 5 );
					expect( orig.getValue() ).toBe( 10 );
					var copy = duplicate( orig );
					expect( copy.getValue() ).toBe( 10 );
					copy.setValue( 7 );
					expect( copy.getValue() ).toBe( 14 );
					expect( orig.getValue() ).toBe( 10 );
				});
				it( title="setter chaining works on duplicate", body=function( currentSpec ){
					var orig = new accessors.testPropertyTypes();
					var copy = duplicate( orig );
					var result = copy.setAge( 30 );
					expect( result.getName() ).toBe( "John" );
				});
				it( title="duplicate() preserves populated collection helpers", body=function( currentSpec ){
					var orig = new accessors.testCollectionArray();
					orig.addTags( "a" );
					orig.addTags( "b" );
					var copy = duplicate( orig );
					expect( copy.hasTags( "a" ) ).toBeTrue();
					expect( copy.hasTags( "b" ) ).toBeTrue();
					expect( arrayLen( copy.getTags() ) ).toBe( 2 );
				});
				it( title="duplicate() isolates populated collections — append on copy does not leak to orig", body=function( currentSpec ){
					var orig = new accessors.testCollectionArray();
					orig.addTags( "shared" );
					var copy = duplicate( orig );
					copy.addTags( "copy-only" );
					expect( orig.hasTags( "copy-only" ) ).toBeFalse();
					expect( copy.hasTags( "shared" ) ).toBeTrue();
					expect( arrayLen( orig.getTags() ) ).toBe( 1 );
					expect( arrayLen( copy.getTags() ) ).toBe( 2 );
				});
				it( title="duplicate() isolates populated collections — remove on copy does not affect orig", body=function( currentSpec ){
					var orig = new accessors.testCollectionArray();
					orig.addTags( "a" );
					orig.addTags( "b" );
					var copy = duplicate( orig );
					copy.removeTags( "a" );
					expect( orig.hasTags( "a" ) ).toBeTrue();
					expect( copy.hasTags( "a" ) ).toBeFalse();
				});
			});

			describe( "concurrent createObject (shared accessor stress)", function(){
				it( title="50 threads × 100 setX on the SAME instance — value is one of the inputs, no corruption", body=function( currentSpec ){
					var cfc = new accessors.testPropertyTypes();
					var threadCount = 50;
					var perThread = 100;
					var threadNames = [];
					for ( var t=1; t<=threadCount; t++ ) {
						var tname = "setage-shared-" & t;
						arrayAppend( threadNames, tname );
						thread name="#tname#" tid=t perThread=perThread sharedCfc=cfc {
							for ( var i=1; i<=attributes.perThread; i++ ) {
								attributes.sharedCfc.setAge( ( attributes.tid * 1000 ) + i );
							}
						}
					}
					thread action="join" name="#arrayToList( threadNames )#";
					for ( var t=1; t<=threadCount; t++ ) {
						var tname = "setage-shared-" & t;
						var th = cfthread[ tname ];
						if ( th.status != "COMPLETED" ) throw( object=th.error );
						structDelete( cfthread, tname );
					}
					// final value must be one of the inputs — no half-state corruption
					var final = cfc.getAge();
					expect( isNumeric( final ) ).toBeTrue();
					expect( final ).toBeGTE( 1001 );
					expect( final ).toBeLTE( ( threadCount * 1000 ) + perThread );
				});
				it( title="50 threads × 10 addX on the SAME instance — all entries land in the array", body=function( currentSpec ){
					// LDEV-6298: regression guard for the DCL lazy-init in UDFAddProperty._call.
					// Was flaky pre-fix (any change that widened cfthread startup parallelism — LDEV-6298 v2, LDEV-5866 — exposed the race).
					var cfc = new accessors.testCollectionArray();
					var threadCount = 50;
					var perThread = 10;
					var threadNames = [];
					for ( var t=1; t<=threadCount; t++ ) {
						var tname = "addtags-shared-" & t;
						arrayAppend( threadNames, tname );
						thread name="#tname#" tid=t perThread=perThread sharedCfc=cfc {
							for ( var i=1; i<=attributes.perThread; i++ ) {
								attributes.sharedCfc.addTags( "t" & attributes.tid & "-i" & i );
							}
						}
					}
					thread action="join" name="#arrayToList( threadNames )#";
					var allTags = cfc.getTags();
					for ( var tname in threadNames ) structDelete( cfthread, tname );
					expect( arrayLen( allTags ) ).toBe( threadCount * perThread );
				});
				it( title="50 threads × 10 addX on the SAME instance (struct property) — all entries land in the struct", body=function( currentSpec ){
					// LDEV-6298: same DCL race, struct/HashMap branch of UDFAddProperty._call.
					// Lazy-init goes to new HashMap(); raw HashMap.put under concurrent writes corrupts without synchronized(propValue).
					var cfc = new accessors.testCollectionStruct();
					var threadCount = 50;
					var perThread = 10;
					var threadNames = [];
					for ( var t=1; t<=threadCount; t++ ) {
						var tname = "addmeta-shared-" & t;
						arrayAppend( threadNames, tname );
						thread name="#tname#" tid=t perThread=perThread sharedCfc=cfc {
							for ( var i=1; i<=attributes.perThread; i++ ) {
								attributes.sharedCfc.addMeta( key="t" & attributes.tid & "-i" & i, meta="v" & attributes.tid & "-" & i );
							}
						}
					}
					thread action="join" name="#arrayToList( threadNames )#";
					var allMeta = cfc.getMeta();
					for ( var tname in threadNames ) structDelete( cfthread, tname );
					expect( structCount( allMeta ) ).toBe( threadCount * perThread );
				});
				it( title="50 threads × 20 fresh addTags — collection helpers isolated per instance", body=function( currentSpec ){
					var threadCount = 50;
					var perThread = 20;
					var threadNames = [];
					for ( var t=1; t<=threadCount; t++ ) {
						var tname = "addtags-stress-" & t;
						arrayAppend( threadNames, tname );
						thread name="#tname#" tid=t perThread=perThread {
							var cfc = new accessors.testCollectionArray();
							for ( var i=1; i<=attributes.perThread; i++ ) {
								cfc.addTags( "t" & attributes.tid & "-i" & i );
							}
							thread.tags = cfc.getTags();
						}
					}
					thread action="join" name="#arrayToList( threadNames )#";
					for ( var t=1; t<=threadCount; t++ ) {
						var tname = "addtags-stress-" & t;
						var th = cfthread[ tname ];
						if ( th.status != "COMPLETED" ) throw( object=th.error );
						expect( arrayLen( th.tags ) ).toBe( perThread );
						// confirm each entry is from the right thread (no cross-thread leakage)
						for ( var entry in th.tags ) {
							expect( entry ).toMatch( "^t" & t & "-i\d+$" );
						}
						structDelete( cfthread, tname );
					}
				});
				it( title="50 threads × 20 fresh instances — property values fully isolated", body=function( currentSpec ){
					var threadCount = 50;
					var perThread = 20;
					var threadNames = [];
					for ( var t=1; t<=threadCount; t++ ) {
						var tname = "accessors-stress-" & t;
						arrayAppend( threadNames, tname );
						thread name="#tname#" tid=t perThread=perThread {
							thread.results = [];
							for ( var i=1; i<=attributes.perThread; i++ ) {
								var inst = new accessors.testPropertyTypes();
								var ageVal = ( attributes.tid * 1000 ) + i;
								inst.setAge( ageVal );
								inst.setName( "t" & attributes.tid & "-i" & i );
								arrayAppend( thread.results, { age: inst.getAge(), name: inst.getName(), expectedAge: ageVal, expectedName: "t" & attributes.tid & "-i" & i } );
							}
						}
					}
					thread action="join" name="#arrayToList( threadNames )#";
					for ( var t=1; t<=threadCount; t++ ) {
						var tname = "accessors-stress-" & t;
						var th = cfthread[ tname ];
						if ( th.status != "COMPLETED" ) throw( object=th.error );
						expect( arrayLen( th.results ) ).toBe( perThread );
						for ( var r in th.results ) {
							expect( r.age ).toBe( r.expectedAge );
							expect( r.name ).toBe( r.expectedName );
						}
						structDelete( cfthread, tname );
					}
				});
			});

		});
	}
}
