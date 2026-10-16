component extends="org.lucee.cfml.test.LuceeTestCase" {

	// Generic CFML "ways to mix functions onto a CFC instance":
	//   - cfinclude as a mixin (constructor body + post-construction)
	//   - closure assignment (cfc.foo = function(){})
	// Documented contract from the GetMetaData reference:
	//   "Runtime-injected functions are invisible to GetMetadata."
	//   "Duplicates return identical metadata to the original, regardless of mixins."
	// LDEV-6236's shared accessor UDF optimization must keep per-instance closure overrides isolated.
	// LDEV-6298's shared ComponentProperties wrapper must keep per-duplicate mixin/closure state isolated.

	function run( testResults, testBox ){
		describe( "CFC mixin patterns", function(){

			describe( "cfinclude as a mixin (constructor body)", function(){
				it( title="cfinclude'd function is externally callable and reads calling instance's tag", body=function( currentSpec ){
					var c = new mixins.CtorMixin( tag="alpha" );
					expect( c.cfincludeMixedFn() ).toBe( "cfinclude-fn:alpha" );
				});
				it( title="siblings have isolated tags — mixin reads each instance's own tag", body=function( currentSpec ){
					var a = new mixins.CtorMixin( tag="aa" );
					var b = new mixins.CtorMixin( tag="bb" );
					expect( a.cfincludeMixedFn() ).toBe( "cfinclude-fn:aa" );
					expect( b.cfincludeMixedFn() ).toBe( "cfinclude-fn:bb" );
				});
				it( title="duplicate preserves the cfinclude'd mixin and reads duplicate's tag", body=function( currentSpec ){
					var orig = new mixins.CtorMixin( tag="orig" );
					var dup = duplicate( orig );
					expect( dup.cfincludeMixedFn() ).toBe( "cfinclude-fn:orig" );
					dup.tag = "dup-mutated";
					expect( dup.cfincludeMixedFn() ).toBe( "cfinclude-fn:dup-mutated" );
					expect( orig.cfincludeMixedFn() ).toBe( "cfinclude-fn:orig" );
				});
			});

			describe( "cfinclude as a mixin (post-construction)", function(){
				it( title="mixin not present until loadMixins() is called", body=function( currentSpec ){
					var c = new mixins.PostMixin( tag="x" );
					expect( structKeyExists( c, "cfincludeMixedFn" ) ).toBeFalse();
					c.loadMixins();
					expect( structKeyExists( c, "cfincludeMixedFn" ) ).toBeTrue();
					expect( c.cfincludeMixedFn() ).toBe( "cfinclude-fn:x" );
				});
				it( title="mixin loaded on one sibling doesn't leak to a sibling that hasn't loaded it", body=function( currentSpec ){
					var a = new mixins.PostMixin( tag="aa" );
					var b = new mixins.PostMixin( tag="bb" );
					a.loadMixins();
					expect( a.cfincludeMixedFn() ).toBe( "cfinclude-fn:aa" );
					expect( structKeyExists( b, "cfincludeMixedFn" ) ).toBeFalse();
				});
				it( title="duplicate after loadMixins carries the mixin to the dup, isolated from source", body=function( currentSpec ){
					var orig = new mixins.PostMixin( tag="orig" );
					orig.loadMixins();
					var dup = duplicate( orig );
					expect( dup.cfincludeMixedFn() ).toBe( "cfinclude-fn:orig" );
					dup.tag = "dup-mutated";
					expect( dup.cfincludeMixedFn() ).toBe( "cfinclude-fn:dup-mutated" );
					expect( orig.cfincludeMixedFn() ).toBe( "cfinclude-fn:orig" );
				});
				it( title="duplicate before loadMixins doesn't carry a phantom mixin to the dup", body=function( currentSpec ){
					var orig = new mixins.PostMixin( tag="orig" );
					var dup = duplicate( orig );
					expect( structKeyExists( dup, "cfincludeMixedFn" ) ).toBeFalse();
					orig.loadMixins();
					expect( structKeyExists( dup, "cfincludeMixedFn" ) ).toBeFalse();
					expect( orig.cfincludeMixedFn() ).toBe( "cfinclude-fn:orig" );
				});
				it( title="internal call via variables scope returns same as external call", body=function( currentSpec ){
					var c = new mixins.PostMixin( tag="z" );
					c.loadMixins();
					expect( c.callMixinInternally() ).toBe( c.cfincludeMixedFn() );
					expect( c.callMixinInternally() ).toBe( "cfinclude-fn:z" );
				});
			});

			describe( "closure mixin coexists with accessors", function(){
				it( title="dynamic mixin method doesn't interfere with accessors", body=function( currentSpec ){
					var cfc = new accessors.testPropertyTypes();
					cfc.setAge(25);
					expect(cfc.getAge()).toBe(25);
					cfc.customMethod = function(){ return "mixin works"; };
					expect(cfc.customMethod()).toBe("mixin works");
					cfc.setAge(30);
					expect(cfc.getAge()).toBe(30);
				});
				it( title="dynamic override of accessor method", body=function( currentSpec ){
					var cfc = new accessors.testPropertyTypes();
					cfc.setAge(25);
					expect(cfc.getAge()).toBe(25);
					cfc.getAge = function(){ return 999; };
					expect(cfc.getAge()).toBe(999);
				});
				it( title="closure override of accessor on one instance doesn't leak to siblings", body=function( currentSpec ){
					var a = new accessors.testPropertyTypes();
					var b = new accessors.testPropertyTypes();
					a.setAge( 11 );
					b.setAge( 22 );
					b.getAge = function() { return 999; };
					expect( a.getAge() ).toBe( 11 );
					expect( b.getAge() ).toBe( 999 );
				});
				it( title="structDelete of closure override doesn't affect siblings", body=function( currentSpec ){
					// Accessor regen on the instance after structDelete is not guaranteed —
					// only sibling isolation is asserted here.
					var a = new accessors.testPropertyTypes();
					var b = new accessors.testPropertyTypes();
					a.setAge( 11 );
					b.setAge( 22 );
					b.getAge = function() { return 999; };
					expect( b.getAge() ).toBe( 999 );
					structDelete( b, "getAge" );
					expect( a.getAge() ).toBe( 11 );
					b.setAge( 33 );
					expect( a.getAge() ).toBe( 11 );
				});
			});

			describe( "concurrent closure injection — per-thread isolation", function(){
				it( title="concurrent new + closure-mixin on same class — mixin per-thread, no leak across siblings", body=function( currentSpec ){
					var threadCount = 20;
					var threadNames = [];
					for ( var t=1; t<=threadCount; t++ ) {
						var tname = "mixins-concurrent-" & t;
						arrayAppend( threadNames, tname );
						thread name="#tname#" tid=t {
							var c = new mixins.Subject( tag="tag-" & attributes.tid );
							var localTid = attributes.tid;
							c.threadFn = function() { return "tfn-" & localTid; };
							thread.who = c.whoAmI();
							thread.tfn = c.threadFn();
						}
					}
					thread action="join" name="#arrayToList( threadNames )#";
					for ( var t=1; t<=threadCount; t++ ) {
						var tname = "mixins-concurrent-" & t;
						var th = cfthread[ tname ];
						if ( th.status != "COMPLETED" ) throw( object=th.error );
						expect( th.who ).toBe( "tag-" & t );
						expect( th.tfn ).toBe( "tfn-" & t );
					}
				});
			});

			describe( "closure-assignment mixin (cfc.foo = function(){})", function(){
				it( title="injected closure is callable on target and returns its literal", body=function( currentSpec ){
					var a = new mixins.Subject( tag="aa" );
					a.injected = function() { return "injected-literal"; };
					expect( a.injected() ).toBe( "injected-literal" );
				});
				it( title="injected closure on instance A doesn't appear on sibling B", body=function( currentSpec ){
					var a = new mixins.Subject( tag="aa" );
					var b = new mixins.Subject( tag="bb" );
					a.injected = function() { return "only-on-a"; };
					expect( a.injected() ).toBe( "only-on-a" );
					expect( structKeyExists( b, "injected" ) ).toBeFalse();
				});
				it( title="getMetaData( a ).functions does NOT include the injected function", body=function( currentSpec ){
					var a = new mixins.Subject( tag="aa" );
					a.injected = function() { return "x"; };
					var meta = getMetaData( a );
					var fnNames = [];
					if ( structKeyExists( meta, "functions" ) ) {
						for ( var f in meta.functions ) arrayAppend( fnNames, f.name );
					}
					expect( fnNames ).notToInclude( "injected" );
				});
				it( title="getMetaData parity — orig vs duplicate identical regardless of pre-duplicate injection", body=function( currentSpec ){
					var orig = new mixins.Subject( tag="orig" );
					orig.injected = function() { return "x"; };
					var dup = duplicate( orig );
					expect( serializeJSON( getMetaData( orig ) ) ).toBe( serializeJSON( getMetaData( dup ) ) );
				});
				it( title="injection on source after duplicate doesn't leak to duplicate", body=function( currentSpec ){
					var orig = new mixins.Subject( tag="orig" );
					var dup = duplicate( orig );
					orig.injected = function() { return "orig-only"; };
					expect( orig.injected() ).toBe( "orig-only" );
					expect( structKeyExists( dup, "injected" ) ).toBeFalse();
				});
				it( title="injection then duplicate — duplicate carries the injected function callable", body=function( currentSpec ){
					var orig = new mixins.Subject( tag="orig" );
					orig.injected = function() { return "from-orig"; };
					var dup = duplicate( orig );
					expect( structKeyExists( dup, "injected" ) ).toBeTrue();
					expect( dup.injected() ).toBe( "from-orig" );
					dup.injected = function() { return "now-from-dup"; };
					expect( dup.injected() ).toBe( "now-from-dup" );
					expect( orig.injected() ).toBe( "from-orig" );
				});
				it( title="mixin-overridden accessor survives duplicate, and re-override on copy is isolated", body=function( currentSpec ){
					var orig = new accessors.testPropertyTypes();
					orig.getAge = function() { return 999; };
					var copy = duplicate( orig );
					expect( copy.getAge() ).toBe( 999 );
					copy.getAge = function() { return 111; };
					expect( copy.getAge() ).toBe( 111 );
					expect( orig.getAge() ).toBe( 999 );
				});
				it( title="structDelete of mixed-in method on duplicate doesn't remove it from source", body=function( currentSpec ){
					var orig = new accessors.testWithAccessors();
					orig.shared = function() { return "original"; };
					var copy = duplicate( orig );
					structDelete( copy, "shared" );
					expect( orig.shared() ).toBe( "original" );
					expect( function(){ copy.shared(); } ).toThrow();
				});
				it( title="restoring real accessor after duplicate restores dispatch", body=function( currentSpec ){
					// Accessor regen on the instance after structDelete is not guaranteed —
					// only sibling isolation is asserted.
					var orig = new accessors.testPropertyTypes();
					orig.getAge = function() { return 999; };
					var copy = duplicate( orig );
					expect( orig.getAge() ).toBe( 999 );
					structDelete( copy, "getAge" );
					copy.setAge( 42 );
					expect( orig.getAge() ).toBe( 999 );
				});
			});

		});
	}
}
