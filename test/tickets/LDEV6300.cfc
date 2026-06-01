component extends="org.lucee.cfml.test.LuceeTestCase" {

	// LDEV-6300 — pre-implementation regression bedrock for the rebuild-from-static work.
	// Pins the CFC inheritance + dispatch contract that the rebuild must preserve, and
	// closes coverage gaps left after LDEV-3335 (static accessor infrastructure),
	// LDEV-6236 (UDFGSProperty share in addUDFS no-owner branch), and LDEV-6298
	// (ComponentProperties wrapper share + duplicateUTFMap UDFGSProperty share).
	//
	// Coverage:
	//   - super.method() dispatch through 3+ levels
	//   - `this` resolves to the calling instance inside an inherited method (the CFML-visible
	//     surface of the runtime's `top` field — phase 2 of the rebuild externalises this)
	//   - onMissingMethod fallback via inheritance after both new() and duplicate()
	//   - direct scope writes (cfc.variables.x / cfc.this.x) survive duplicate
	//   - concurrent createObject of the same class (sibling to LDEV6298's concurrent duplicate;
	//     phase 3 of the rebuild shares the cached base across instantiations)
	//   - multi-level property pulldown across 3-level extends (mappedSuperClass shape)
	//   - ThreadLocalDuplication re-entry on self-referential CFCs

	function run( testResults, testBox ){
		describe( title="LDEV-6300 ComponentImpl rebuild-from-static regression bedrock", body=function(){

			describe( title="3-level super dispatch", body=function(){
				it( title="super.super chain returns each level joined", body=function( currentSpec ){
					var c = new LDEV6300.Child();
					expect( c.chain() ).toBe( "grand:parent:child" );
				});
				it( title="duplicate of a 3-level child preserves super chain", body=function( currentSpec ){
					var c = new LDEV6300.Child();
					var d = duplicate( c );
					expect( d.chain() ).toBe( "grand:parent:child" );
				});
				it( title="50 sequential new Child() — every chain() resolves identically", body=function( currentSpec ){
					for ( var i=1; i<=50; i++ ) {
						var c = new LDEV6300.Child();
						expect( c.chain() ).toBe( "grand:parent:child" );
					}
				});
			});

			describe( title="`this` inside inherited method resolves to calling child", body=function(){
				it( title="whoAmI() defined on Grandparent reads child's tag", body=function( currentSpec ){
					var c = new LDEV6300.Child( tag="alpha" );
					expect( c.whoAmI() ).toBe( "alpha" );
				});
				it( title="two siblings of same class — each whoAmI() returns own tag", body=function( currentSpec ){
					var a = new LDEV6300.Child( tag="aa" );
					var b = new LDEV6300.Child( tag="bb" );
					expect( a.whoAmI() ).toBe( "aa" );
					expect( b.whoAmI() ).toBe( "bb" );
				});
				it( title="duplicate then mutate tag — whoAmI() reflects the duplicate's tag", body=function( currentSpec ){
					var orig = new LDEV6300.Child( tag="orig" );
					var dup = duplicate( orig );
					dup.tag = "dup";
					expect( orig.whoAmI() ).toBe( "orig" );
					expect( dup.whoAmI() ).toBe( "dup" );
				});
				it( title="tagMe() inherited from Grandparent writes to calling child's scope", body=function( currentSpec ){
					var a = new LDEV6300.Child( tag="a-init" );
					var b = new LDEV6300.Child( tag="b-init" );
					a.tagMe( "a-after" );
					expect( a.whoAmI() ).toBe( "a-after" );
					expect( b.whoAmI() ).toBe( "b-init" );
				});
			});

			describe( title="onMissingMethod fallback through inheritance", body=function(){
				it( title="missing method on child falls through to parent's oMM", body=function( currentSpec ){
					var c = new LDEV6300.OnMissingChild( tag="om1" );
					expect( c.declared() ).toBe( "declared:om1" );
					expect( c.somethingMissing() ).toBe( "om1:somethingMissing" );
				});
				it( title="duplicate of OnMissingChild — declared and oMM both still work", body=function( currentSpec ){
					var c = new LDEV6300.OnMissingChild( tag="om2" );
					var d = duplicate( c );
					expect( d.declared() ).toBe( "declared:om2" );
					expect( d.somethingMissing() ).toBe( "om2:somethingMissing" );
				});
				it( title="oMM reads `this.tag` from the calling instance, not the base", body=function( currentSpec ){
					var a = new LDEV6300.OnMissingChild( tag="aa" );
					var b = new LDEV6300.OnMissingChild( tag="bb" );
					expect( a.fooBar() ).toBe( "aa:fooBar" );
					expect( b.fooBar() ).toBe( "bb:fooBar" );
				});
			});

			describe( title="direct scope writes survive duplicate", body=function(){
				it( title="cfc.variables.x assigned then duplicated — duplicate has independent value", body=function( currentSpec ){
					var orig = new LDEV3335.testWithAccessors();
					orig.variables.adhoc = "orig-adhoc";
					var dup = duplicate( orig );
					expect( dup.variables.adhoc ).toBe( "orig-adhoc" );
					dup.variables.adhoc = "dup-adhoc";
					expect( orig.variables.adhoc ).toBe( "orig-adhoc" );
					expect( dup.variables.adhoc ).toBe( "dup-adhoc" );
				});
				it( title="cfc.this.x assigned then duplicated — duplicate has independent value", body=function( currentSpec ){
					var orig = new LDEV3335.testWithAccessors();
					orig.adhoc = "orig-this";
					var dup = duplicate( orig );
					expect( dup.adhoc ).toBe( "orig-this" );
					dup.adhoc = "dup-this";
					expect( orig.adhoc ).toBe( "orig-this" );
					expect( dup.adhoc ).toBe( "dup-this" );
				});
			});

			describe( title="concurrent createObject of same class (init-side stress)", body=function(){
				it( title="50 threads × 20 fresh Child() — chain + whoAmI consistent per thread", body=function( currentSpec ){
					var threadCount = 50;
					var perThread = 20;
					var threadNames = [];
					for ( var t=1; t<=threadCount; t++ ) {
						var tname = "ldev6300-init-" & t;
						arrayAppend( threadNames, tname );
						thread name="#tname#" tid=t perThread=perThread {
							thread.results = [];
							for ( var i=1; i<=attributes.perThread; i++ ) {
								var tag = "t" & attributes.tid & "-i" & i;
								var c = new LDEV6300.Child( tag=tag );
								arrayAppend( thread.results, { chain: c.chain(), who: c.whoAmI(), expectedTag: tag } );
							}
						}
					}
					thread action="join" name="#arrayToList( threadNames )#";
					for ( var t=1; t<=threadCount; t++ ) {
						var tname = "ldev6300-init-" & t;
						var th = cfthread[ tname ];
						if ( th.status != "COMPLETED" ) throw( object=th.error );
						expect( arrayLen( th.results ) ).toBe( perThread );
						for ( var r in th.results ) {
							expect( r.chain ).toBe( "grand:parent:child" );
							expect( r.who ).toBe( r.expectedTag );
						}
					}
				});
				it( title="concurrent new + mixin on same class — mixin per-thread, no leak across siblings", body=function( currentSpec ){
					var threadCount = 20;
					var threadNames = [];
					for ( var t=1; t<=threadCount; t++ ) {
						var tname = "ldev6300-mix-" & t;
						arrayAppend( threadNames, tname );
						thread name="#tname#" tid=t {
							var c = new LDEV6300.Child( tag="tag-" & attributes.tid );
							c.threadFn = function() { return "tfn-" & attributes.tid; };
							thread.who = c.whoAmI();
							thread.tfn = c.threadFn();
							thread.chain = c.chain();
						}
					}
					thread action="join" name="#arrayToList( threadNames )#";
					for ( var t=1; t<=threadCount; t++ ) {
						var tname = "ldev6300-mix-" & t;
						var th = cfthread[ tname ];
						if ( th.status != "COMPLETED" ) throw( object=th.error );
						expect( th.who ).toBe( "tag-" & t );
						expect( th.tfn ).toBe( "tfn-" & t );
						expect( th.chain ).toBe( "grand:parent:child" );
					}
				});
				it( title="concurrent new while siblings mutate `this` scope — no cross-thread bleed", body=function( currentSpec ){
					// Phase 3 of the rebuild-from-static spec shares the cached base across
					// instantiations. This stress test fires `new` + scope writes against that
					// shared-base class concurrently to surface any race in per-instance setup.
					var threadCount = 30;
					var perThread = 10;
					var threadNames = [];
					for ( var t=1; t<=threadCount; t++ ) {
						var tname = "ldev6300-scope-" & t;
						arrayAppend( threadNames, tname );
						thread name="#tname#" tid=t perThread=perThread {
							thread.results = [];
							for ( var i=1; i<=attributes.perThread; i++ ) {
								var c = new LDEV6300.Child( tag="t" & attributes.tid & "-" & i );
								c.variables.adhoc = "av-" & attributes.tid & "-" & i;
								c.adhocThis = "at-" & attributes.tid & "-" & i;
								arrayAppend( thread.results, {
									who: c.whoAmI(),
									av: c.variables.adhoc,
									at: c.adhocThis,
									expectedTag: "t" & attributes.tid & "-" & i,
									expectedAv: "av-" & attributes.tid & "-" & i,
									expectedAt: "at-" & attributes.tid & "-" & i
								} );
							}
						}
					}
					thread action="join" name="#arrayToList( threadNames )#";
					for ( var t=1; t<=threadCount; t++ ) {
						var tname = "ldev6300-scope-" & t;
						var th = cfthread[ tname ];
						if ( th.status != "COMPLETED" ) throw( object=th.error );
						expect( arrayLen( th.results ) ).toBe( perThread );
						for ( var r in th.results ) {
							expect( r.who ).toBe( r.expectedTag );
							expect( r.av ).toBe( r.expectedAv );
							expect( r.at ).toBe( r.expectedAt );
						}
					}
				});
			});

			describe( title="multi-level property pulldown (mappedSuperClass shape in plain CFML)", body=function(){
				it( title="3-level extends — accessors at every level dispatch on the leaf", body=function( currentSpec ){
					var c = new LDEV6300.Child();
					expect( c.getGprop() ).toBe( "g-default" );
					expect( c.getPprop() ).toBe( "p-default" );
					expect( c.getCprop() ).toBe( "c-default" );
					c.setGprop( "g-set" );
					c.setPprop( "p-set" );
					c.setCprop( "c-set" );
					expect( c.getGprop() ).toBe( "g-set" );
					expect( c.getPprop() ).toBe( "p-set" );
					expect( c.getCprop() ).toBe( "c-set" );
				});
				it( title="duplicate of leaf — accessors at every level still dispatch", body=function( currentSpec ){
					var c = new LDEV6300.Child();
					c.setGprop( "g1" );
					c.setPprop( "p1" );
					c.setCprop( "c1" );
					var d = duplicate( c );
					expect( d.getGprop() ).toBe( "g1" );
					expect( d.getPprop() ).toBe( "p1" );
					expect( d.getCprop() ).toBe( "c1" );
					// mutations on dup don't leak to source at any level
					d.setGprop( "g2" );
					d.setPprop( "p2" );
					d.setCprop( "c2" );
					expect( c.getGprop() ).toBe( "g1" );
					expect( c.getPprop() ).toBe( "p1" );
					expect( c.getCprop() ).toBe( "c1" );
				});
				it( title="getMetadata on leaf surfaces inherited properties from every level", body=function( currentSpec ){
					var c = new LDEV6300.Child();
					var meta = getMetadata( c );
					var propNames = [];
					for ( var p in meta.properties ) arrayAppend( propNames, p.name );
					// own
					expect( propNames ).toInclude( "cprop" );
					// inherited via super metadata
					var allNames = propNames;
					var cur = meta;
					while ( structKeyExists( cur, "extends" ) ) {
						cur = cur.extends;
						if ( structKeyExists( cur, "properties" ) ) {
							for ( var p in cur.properties ) arrayAppend( allNames, p.name );
						}
					}
					expect( allNames ).toInclude( "gprop" );
					expect( allNames ).toInclude( "pprop" );
					expect( allNames ).toInclude( "cprop" );
				});
			});

			describe( title="cfinclude as a mixin path (parallel to closure-assignment mixin)", body=function(){
				// Closure-assignment mixin (cfc.foo = function(){}) routes through _set, lands in
				// _data + _udfs, flips hasInjectedFunctions. cfinclude routes differently:
				//   - inside init() / constructor body → ComponentScopeShadow.set with
				//     !afterConstructor → addConstructorUDF → registerUDF → _udfs + shadow
				//   - inside a post-construction method → ComponentScopeShadow.set with
				//     afterConstructor=true → shadow.put (probe confirmed externally visible too)
				// Neither path flips hasInjectedFunctions. These tests pin the externally-visible
				// dispatch + isolation semantics so phase 4 of the rebuild can change scope/UDF
				// wiring without silently breaking cfinclude-based mixin libraries.
				describe( title="constructor cfinclude (init body)", body=function(){
					it( title="cfinclude'd function is externally callable and reads calling instance's tag", body=function( currentSpec ){
						var c = new LDEV6300.CtorMixin( tag="alpha" );
						expect( c.cfincludeMixedFn() ).toBe( "cfinclude-fn:alpha" );
					});
					it( title="siblings have isolated tags — mixin reads each instance's own tag", body=function( currentSpec ){
						var a = new LDEV6300.CtorMixin( tag="aa" );
						var b = new LDEV6300.CtorMixin( tag="bb" );
						expect( a.cfincludeMixedFn() ).toBe( "cfinclude-fn:aa" );
						expect( b.cfincludeMixedFn() ).toBe( "cfinclude-fn:bb" );
					});
					it( title="duplicate preserves the cfinclude'd mixin and reads duplicate's tag", body=function( currentSpec ){
						var orig = new LDEV6300.CtorMixin( tag="orig" );
						var dup = duplicate( orig );
						expect( dup.cfincludeMixedFn() ).toBe( "cfinclude-fn:orig" );
						dup.tag = "dup-mutated";
						expect( dup.cfincludeMixedFn() ).toBe( "cfinclude-fn:dup-mutated" );
						expect( orig.cfincludeMixedFn() ).toBe( "cfinclude-fn:orig" );
					});
				});

				describe( title="post-construction cfinclude (method body)", body=function(){
					it( title="mixin not present until loadMixins() is called", body=function( currentSpec ){
						var c = new LDEV6300.PostMixin( tag="x" );
						expect( structKeyExists( c, "cfincludeMixedFn" ) ).toBeFalse();
						c.loadMixins();
						expect( structKeyExists( c, "cfincludeMixedFn" ) ).toBeTrue();
						expect( c.cfincludeMixedFn() ).toBe( "cfinclude-fn:x" );
					});
					it( title="mixin loaded on one sibling doesn't leak to a sibling that hasn't loaded it", body=function( currentSpec ){
						var a = new LDEV6300.PostMixin( tag="aa" );
						var b = new LDEV6300.PostMixin( tag="bb" );
						a.loadMixins();
						expect( a.cfincludeMixedFn() ).toBe( "cfinclude-fn:aa" );
						expect( structKeyExists( b, "cfincludeMixedFn" ) ).toBeFalse();
					});
					it( title="duplicate after loadMixins carries the mixin to the dup, isolated from source", body=function( currentSpec ){
						var orig = new LDEV6300.PostMixin( tag="orig" );
						orig.loadMixins();
						var dup = duplicate( orig );
						expect( dup.cfincludeMixedFn() ).toBe( "cfinclude-fn:orig" );
						dup.tag = "dup-mutated";
						expect( dup.cfincludeMixedFn() ).toBe( "cfinclude-fn:dup-mutated" );
						expect( orig.cfincludeMixedFn() ).toBe( "cfinclude-fn:orig" );
					});
					it( title="duplicate before loadMixins doesn't carry a phantom mixin to the dup", body=function( currentSpec ){
						var orig = new LDEV6300.PostMixin( tag="orig" );
						var dup = duplicate( orig );
						expect( structKeyExists( dup, "cfincludeMixedFn" ) ).toBeFalse();
						orig.loadMixins();
						// loading mixin on source post-duplicate doesn't leak to dup
						expect( structKeyExists( dup, "cfincludeMixedFn" ) ).toBeFalse();
						expect( orig.cfincludeMixedFn() ).toBe( "cfinclude-fn:orig" );
					});
					it( title="internal call via variables scope returns same as external call", body=function( currentSpec ){
						var c = new LDEV6300.PostMixin( tag="z" );
						c.loadMixins();
						expect( c.callMixinInternally() ).toBe( c.cfincludeMixedFn() );
						expect( c.callMixinInternally() ).toBe( "cfinclude-fn:z" );
					});
				});
			});

			describe( title="ThreadLocalDuplication re-entry (self-referential CFC)", body=function(){
				// Identity is asserted via mutation-through-reference rather than equality compare,
				// because TestBox's toBe() recurses through CFC content and stack-overflows on cycles.
				it( title="duplicate of CFC holding reference to itself doesn't infinite-loop", body=function( currentSpec ){
					var c = new LDEV6300.Child( tag="root" );
					c.self = c;
					var d = duplicate( c );
					expect( d.tag ).toBe( "root" );
					// self-ref on the duplicate must point at the duplicate, not the source:
					// mutating via d.self should mutate d, not c
					d.marker = "before";
					d.self.marker = "via-self";
					expect( d.marker ).toBe( "via-self" );
					// and the source is untouched
					expect( structKeyExists( c, "marker" ) ).toBeFalse();
				});
				it( title="duplicate of two-CFC cycle (a.peer = b, b.peer = a) — both duplicates form a cycle of their own", body=function( currentSpec ){
					var a = new LDEV6300.Child( tag="a" );
					var b = new LDEV6300.Child( tag="b" );
					a.peer = b;
					b.peer = a;
					var dupA = duplicate( a );
					expect( dupA.tag ).toBe( "a" );
					expect( dupA.peer.tag ).toBe( "b" );
					// the cycle is reconstructed on the duplicate side, not crossed back to source.
					// dupA.peer.peer must point at dupA — verify by mutating through the cycle and
					// checking dupA observes the mutation but a does not.
					dupA.peer.peer.marker = "via-cycle";
					expect( dupA.marker ).toBe( "via-cycle" );
					expect( structKeyExists( a, "marker" ) ).toBeFalse();
					expect( structKeyExists( b, "marker" ) ).toBeFalse();
				});
				it( title="concurrent self-referential duplicates — ThreadLocal scoped per thread", body=function( currentSpec ){
					var threadCount = 20;
					var threadNames = [];
					for ( var t=1; t<=threadCount; t++ ) {
						var tname = "ldev6300-reentry-" & t;
						arrayAppend( threadNames, tname );
						thread name="#tname#" tid=t {
							var c = new LDEV6300.Child( tag="t-" & attributes.tid );
							c.self = c;
							var d = duplicate( c );
							// mutate via self-ref on the duplicate
							d.self.marker = "m-" & attributes.tid;
							thread.tagOut = d.tag;
							thread.markerOut = d.marker;
							thread.sourceUntouched = !structKeyExists( c, "marker" );
						}
					}
					thread action="join" name="#arrayToList( threadNames )#";
					for ( var t=1; t<=threadCount; t++ ) {
						var tname = "ldev6300-reentry-" & t;
						var th = cfthread[ tname ];
						if ( th.status != "COMPLETED" ) throw( object=th.error );
						expect( th.tagOut ).toBe( "t-" & t );
						expect( th.markerOut ).toBe( "m-" & t );
						expect( th.sourceUntouched ).toBeTrue();
					}
				});
			});

		});
	}
}
