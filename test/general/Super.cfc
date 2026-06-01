component extends="org.lucee.cfml.test.LuceeTestCase" {

	// `super` keyword contract surface — reads, writes, identity, closure capture,
	// onMissingMethod fallthrough, and sibling isolation under base-instance mutation.
	//
	// `super.method()` calls are pinned by `test/general/Inheritance.cfc`; this file
	// covers the rest of the contract that wasn't being asserted before:
	//
	//   - read patterns through super (dot, bracket, struct functions, getMetaData)
	//   - write patterns that reach SuperComponent's set / setEL / structClear /
	//     structDelete delegators (sibling isolation under today's duplicated base)
	//   - super captured into a variable, super inside closures and lambdas
	//   - super.X falling through to super's onMissingMethod
	//   - isInstanceOf(super, X) — class-hierarchy identity through super
	//
	// Phase 3 of LDEV-6300 collapses the implicit Component.cfc base duplicate to a
	// shared ref. Today's "sibling isolation" comes from each child holding its own
	// duplicated base; phase 3 must either preserve that or break it with explicit
	// documentation.
	//
	// Recent context: Lucee 7.0.2.54+ fixed a real super-dispatch regression where
	// `super.method()` resolution returned a null reference without validating member
	// existence (Preside startup failure). This file's coverage is partly motivated
	// by how thin the existing super contract was at the time of that regression.

	function run( testResults, testBox ){
		describe( "super keyword — reads", function(){

			it( title="super.basePublicMethod() dispatches to the parent's method", body=function( currentSpec ){
				var c = new super.Child();
				expect( c.readSuperMethod() ).toBe( "base-public-method-result" );
			});

			it( title="super.basePrivateMethod() can call private parent methods from the child", body=function( currentSpec ){
				var c = new super.Child();
				expect( c.readSuperPrivateMethod() ).toBe( "base-private-method-result" );
			});

			it( title="super.basePublicProp reads the parent's property default", body=function( currentSpec ){
				var c = new super.Child();
				expect( c.readSuperPublicProp() ).toBe( "base-public-default" );
			});

			it( title="super[ ""key"" ] bracket-syntax read resolves the parent's value", body=function( currentSpec ){
				var c = new super.Child();
				expect( c.readSuperBracket( "basePublicProp" ) ).toBe( "base-public-default" );
			});

			it( title="structKeyExists(super, ""basePublicMethod"") finds inherited method", body=function( currentSpec ){
				var c = new super.Child();
				expect( c.structKeyExistsOnSuper( "basePublicMethod" ) ).toBeTrue();
			});

			it( title="structKeyExists(super, ""nonExistent"") returns false for missing keys", body=function( currentSpec ){
				var c = new super.Child();
				expect( c.structKeyExistsOnSuper( "nothingHere" ) ).toBeFalse();
			});

		});

		describe( "super keyword — identity and metadata", function(){

			it( title="isInstanceOf(super, ""Base"") returns true", body=function( currentSpec ){
				var c = new super.Child();
				expect( c.isSuperInstanceOf( "Base" ) ).toBeTrue();
			});

			it( title="isInstanceOf(super, ""Child"") ALSO returns true — SuperComponent.instanceOf delegates to the calling CFC instance's identity", body=function( currentSpec ){
				// Surprising but real: SuperComponent.instanceOf(type) calls `comp.top.instanceOf(type)`
				// (SuperComponent.java:282), where `comp.top` is the calling CFC instance. So
				// `isInstanceOf(super, X)` effectively asks "is the CFC instance an instance of X" —
				// not "is the parent class". CFML developers who expect "super = parent identity"
				// get a counter-intuitive answer. Pin the actual behaviour; document the surprise.
				var c = new super.Child();
				expect( c.isSuperInstanceOf( "Child" ) ).toBeTrue();
			});

			it( title="getMetaData(super) reports the calling CFC instance's metadata — same `comp.top` delegation pattern", body=function( currentSpec ){
				// Same shape as isInstanceOf(super, X): getMetaData(super) returns the calling
				// CFC instance's class metadata, not the parent's. SuperComponent dispatches
				// metadata through `comp.top`. Pin actual.
				var c = new super.Child();
				var meta = c.metaDataOfSuper();
				expect( right( meta.name, len( ".super.Child" ) ) ).toBe( ".super.Child" );
			});

		});

		describe( "super keyword — closure and lambda capture", function(){

			it( title="super.basePublicMethod() inside a closure resolves to the parent's method", body=function( currentSpec ){
				var c = new super.Child();
				expect( c.superInClosure() ).toBe( "base-public-method-result" );
			});

			it( title="super.basePublicMethod() inside an arrow-lambda resolves to the parent's method", body=function( currentSpec ){
				var c = new super.Child();
				expect( c.superInLambda() ).toBe( "base-public-method-result" );
			});

			it( title="super.basePublicMethod() inside nested closures still resolves correctly", body=function( currentSpec ){
				var c = new super.Child();
				expect( c.superInNestedClosure() ).toBe( "base-public-method-result" );
			});

			it( title="var s = super; s.basePublicMethod() — captured super reference dispatches", body=function( currentSpec ){
				var c = new super.Child();
				expect( c.superCapturedToVariable() ).toBe( "base-public-method-result" );
			});

			it( title="super reference returned from a method can still dispatch", body=function( currentSpec ){
				var c = new super.Child();
				var s = c.returnSuperReference();
				expect( s.basePublicMethod() ).toBe( "base-public-method-result" );
			});

			it( title="LDEV-976 shape: super reference returned inside an array still dispatches", body=function( currentSpec ){
				var c = new super.Child();
				var arr = c.returnSuperInArray();
				expect( arr[ 1 ].basePublicMethod() ).toBe( "base-public-method-result" );
				expect( arr[ 2 ] ).toBe( "marker" );
			});

		});

		describe( "super keyword — onMissingMethod fallthrough", function(){

			it( title="super.someMethodThatDoesNotExist() falls through to base's onMissingMethod", body=function( currentSpec ){
				var c = new super.Child();
				expect( c.callSuperNonExistentMethod() ).toBe( "base-omm:someMethodThatDoesNotExist" );
			});

		});

		describe( "super write delegators — sibling isolation", function(){

			it( title="super.foo = ... on A leaves B's super.foo untouched", body=function( currentSpec ){
				var a = new super.Plain();
				var b = new super.Plain();
				a.writeSuperFoo( "value-from-A" );
				expect( a.readSuperFoo() ).toBe( "value-from-A" );
				expect( b.readSuperKeyOrDefault( "foo", "<unset>" ) ).toBe( "<unset>" );
			});

			it( title="super[ ""key"" ] = ... bracket syntax is sibling-isolated", body=function( currentSpec ){
				var a = new super.Plain();
				var b = new super.Plain();
				a.writeSuperKey( "bracketKey", "bracket-A" );
				expect( a.readSuperKey( "bracketKey" ) ).toBe( "bracket-A" );
				expect( b.readSuperKeyOrDefault( "bracketKey", "<unset>" ) ).toBe( "<unset>" );
			});

			it( title="structDelete(super, key) on A doesn't remove B's same-named key", body=function( currentSpec ){
				// Same aliasing caveat as the structClear test — A's state after remove
				// is governed by today's `_data = base._data` aliasing, so we only assert B.
				var a = new super.Plain();
				var b = new super.Plain();
				a.writeSuperKey( "toRemove", "a-val" );
				b.writeSuperKey( "toRemove", "b-val" );
				a.removeSuperKey( "toRemove" );
				expect( b.readSuperKey( "toRemove" ) ).toBe( "b-val" );
			});

			it( title="structClear(super) on A doesn't wipe B's super state", body=function( currentSpec ){
				// Asserts B's state only — A's state after the clear is currently ill-defined
				// because today's `_data = base._data` aliasing means clearing super wipes the
				// CFC instance's UDF map too. That aliasing is a phase-3 fix target (LDEV-6300).
				var a = new super.Plain();
				var b = new super.Plain();
				a.writeSuperKey( "preserved", "set-on-a" );
				b.writeSuperKey( "preserved", "set-on-b" );
				a.clearSuper();
				expect( b.readSuperKey( "preserved" ) ).toBe( "set-on-b" );
			});

			it( title="structClear(super) on A doesn't wipe a fresh sibling C created after the clear", body=function( currentSpec ){
				// Phase 3 hazard shape: if A's super.clear() hit the cached base directly,
				// every subsequent instantiation of Plain would see an empty base.
				var a = new super.Plain();
				a.writeSuperKey( "before", "x" );
				a.clearSuper();
				var c = new super.Plain();
				c.writeSuperKey( "fresh", "y" );
				expect( c.readSuperKey( "fresh" ) ).toBe( "y" );
			});

			it( title="20 threads × super.foo = thread-tag — every thread reads its own write back", body=function( currentSpec ){
				// Concurrent stress version of the basic isolation test. If any thread's
				// write leaks to another thread's super, the read-back would see the
				// wrong value. Tripwire for any phase 3 regression that introduces
				// shared base mutation under contention.
				var threadCount = 20;
				var threadNames = [];
				for ( var t=1; t<=threadCount; t++ ) {
					var tname = "super-isolation-" & t;
					arrayAppend( threadNames, tname );
					thread name="#tname#" tid=t {
						var p = new super.Plain();
						p.writeSuperFoo( "thread-" & attributes.tid );
						thread.read = p.readSuperFoo();
					}
				}
				thread action="join" name="#arrayToList( threadNames )#";
				for ( var t=1; t<=threadCount; t++ ) {
					var tname = "super-isolation-" & t;
					var th = cfthread[ tname ];
					if ( th.status != "COMPLETED" ) throw( object=th.error );
					expect( th.read ).toBe( "thread-" & t );
				}
			});

		});

		describe( "super keyword — `this` dispatch resolves to the calling CFC instance", function(){

			// 3-level case is pinned by Inheritance.cfc; this is the 2-level case for
			// self-containment of this file.

			it( title="super.whoAmIFromBase() reads `this.tag` from the calling Child", body=function( currentSpec ){
				var c = new super.Child( tag="cfc-instance-tag" );
				// whoAmIFromBase is defined on Base and reads `this.tag` — when dispatched
				// via super from Child, `this` must resolve to Child, so this.tag is the
				// CFC instance's tag.
				var s = c.returnSuperReference();
				expect( s.whoAmIFromBase() ).toBe( "cfc-instance-tag" );
			});

			it( title="two Child siblings — super.whoAmIFromBase() reads each CFC instance's own tag", body=function( currentSpec ){
				var a = new super.Child( tag="aa" );
				var b = new super.Child( tag="bb" );
				expect( a.returnSuperReference().whoAmIFromBase() ).toBe( "aa" );
				expect( b.returnSuperReference().whoAmIFromBase() ).toBe( "bb" );
			});

		});

		describe( "super keyword — duplicate interaction", function(){

			it( title="duplicate(child).super.basePublicMethod() still dispatches", body=function( currentSpec ){
				var orig = new super.Child( tag="orig" );
				var dup = duplicate( orig );
				expect( dup.readSuperMethod() ).toBe( "base-public-method-result" );
			});

			it( title="duplicate(child) — super.whoAmIFromBase() reads duplicate's tag, not original's", body=function( currentSpec ){
				var orig = new super.Child( tag="orig" );
				var dup = duplicate( orig );
				dup.tag = "dup";
				expect( orig.returnSuperReference().whoAmIFromBase() ).toBe( "orig" );
				expect( dup.returnSuperReference().whoAmIFromBase() ).toBe( "dup" );
			});

		});
	}
}
