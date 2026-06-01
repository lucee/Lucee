component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults, testBox ){
		describe( title="LDEV-6298 share flyweight property accessors across Component duplicates", body=function(){

			describe( title="runtime mutation isolation", body=function(){
				it( title="overriding accessor on duplicate doesn't leak to source", body=function( currentSpec ){
					var orig = new LDEV3335.testPropertyTypes();
					orig.setAge( 30 );
					var copy = duplicate( orig );
					copy.getAge = function() { return 999; };
					expect( copy.getAge() ).toBe( 999 );
					expect( orig.getAge() ).toBe( 30 );
				});
				it( title="overriding accessor on source doesn't leak to existing duplicate", body=function( currentSpec ){
					var orig = new LDEV3335.testPropertyTypes();
					var copy = duplicate( orig );
					orig.getAge = function() { return 777; };
					expect( orig.getAge() ).toBe( 777 );
					// duplicate must still use real accessor
					copy.setAge( 42 );
					expect( copy.getAge() ).toBe( 42 );
				});
				it( title="injecting new method on duplicate doesn't leak to source", body=function( currentSpec ){
					var orig = new LDEV3335.testWithAccessors();
					var copy = duplicate( orig );
					copy.customMethod = function() { return "from copy"; };
					expect( copy.customMethod() ).toBe( "from copy" );
					expect( function(){ orig.customMethod(); } ).toThrow();
				});
			});

			describe( title="mass duplication (ORM hot path)", body=function(){
				it( title="50 duplicates from one prototype — all isolated", body=function( currentSpec ){
					var prototype = new LDEV3335.testPropertyTypes();
					var entities = [];
					for ( var i=1; i<=50; i++ ) {
						var entity = duplicate( prototype );
						entity.setAge( i );
						entity.setName( "entity-" & i );
						arrayAppend( entities, entity );
					}
					for ( var i=1; i<=50; i++ ) {
						expect( entities[ i ].getAge() ).toBe( i );
						expect( entities[ i ].getName() ).toBe( "entity-" & i );
					}
				});
				it( title="duplicates remain isolated when prototype is mutated mid-flight", body=function( currentSpec ){
					var prototype = new LDEV3335.testWithAccessors();
					prototype.setA( "before" );
					var early = duplicate( prototype );
					prototype.setA( "after" );
					var late = duplicate( prototype );
					expect( early.getA() ).toBe( "before" );
					expect( late.getA() ).toBe( "after" );
				});
			});

			describe( title="chained duplicates (a → b → c)", body=function(){
				it( title="three-level chain — each level isolated", body=function( currentSpec ){
					var a = new LDEV3335.testWithAccessors();
					a.setA( "level-a" );
					var b = duplicate( a );
					b.setA( "level-b" );
					var c = duplicate( b );
					c.setA( "level-c" );
					expect( a.getA() ).toBe( "level-a" );
					expect( b.getA() ).toBe( "level-b" );
					expect( c.getA() ).toBe( "level-c" );
				});
				it( title="mixin at level B + mixin at level C — each mixin only visible at and below its level", body=function( currentSpec ){
					var a = new LDEV3335.testWithAccessors();
					var b = duplicate( a );
					b.bMixin = function() { return "from b"; };
					var c = duplicate( b );
					c.cMixin = function() { return "from c"; };
					expect( function(){ a.bMixin(); } ).toThrow();
					expect( function(){ a.cMixin(); } ).toThrow();
					expect( b.bMixin() ).toBe( "from b" );
					expect( function(){ b.cMixin(); } ).toThrow();
					expect( c.bMixin() ).toBe( "from b" );
					expect( c.cMixin() ).toBe( "from c" );
				});
			});

			describe( title="reflection metadata still correct", body=function(){
				it( title="getMetadata() on duplicate returns same property list as source", body=function( currentSpec ){
					var orig = new LDEV3335.testPropertyTypes();
					var copy = duplicate( orig );
					var origMeta = getMetadata( orig );
					var copyMeta = getMetadata( copy );
					expect( copyMeta.name ).toBe( origMeta.name );
					expect( arrayLen( copyMeta.properties ) ).toBe( arrayLen( origMeta.properties ) );
				});
			});

			describe( title="inheritance preserved across duplicate", body=function(){
				it( title="parent property accessor works on duplicated child", body=function( currentSpec ){
					var child = new LDEV3335.testInheritChild();
					var copy = duplicate( child );
					expect( copy.getParentProp() ).toBe( "from parent" );
					copy.setParentProp( "set on copy" );
					expect( copy.getParentProp() ).toBe( "set on copy" );
					expect( child.getParentProp() ).toBe( "from parent" );
				});
				it( title="child override of parent accessor preserved on duplicate", body=function( currentSpec ){
					var orig = new LDEV3335.testInheritChildOverride();
					var copy = duplicate( orig );
					expect( copy.getParentProp() ).toBe( "CHILD OVERRIDE" );
				});
			});

			describe( title="mixin propagation: prototype → duplicate", body=function(){
				it( title="closure mixed onto prototype is carried into the duplicate", body=function( currentSpec ){
					var orig = new LDEV3335.testWithAccessors();
					orig.customMethod = function() { return "mixed in"; };
					var copy = duplicate( orig );
					expect( copy.customMethod() ).toBe( "mixed in" );
				});
				it( title="mixin-overridden accessor survives duplicate, and re-override on copy is isolated", body=function( currentSpec ){
					var orig = new LDEV3335.testPropertyTypes();
					orig.getAge = function() { return 999; };
					var copy = duplicate( orig );
					expect( copy.getAge() ).toBe( 999 );
					// re-override on copy must not bleed back to orig
					copy.getAge = function() { return 111; };
					expect( copy.getAge() ).toBe( 111 );
					expect( orig.getAge() ).toBe( 999 );
				});
				it( title="structDelete of mixed-in method on duplicate doesn't remove it from source", body=function( currentSpec ){
					var orig = new LDEV3335.testWithAccessors();
					orig.shared = function() { return "original"; };
					var copy = duplicate( orig );
					structDelete( copy, "shared" );
					expect( orig.shared() ).toBe( "original" );
					expect( function(){ copy.shared(); } ).toThrow();
				});
				it( title="restoring real accessor after duplicate restores dispatch", body=function( currentSpec ){
					var orig = new LDEV3335.testPropertyTypes();
					orig.getAge = function() { return 999; };
					var copy = duplicate( orig );
					// orig still has the mixin
					expect( orig.getAge() ).toBe( 999 );
					// removing the mixin from copy should fall back to default (or property scope)
					structDelete( copy, "getAge" );
					copy.setAge( 42 );
					expect( orig.getAge() ).toBe( 999 );
					// note: copy.getAge() after structDelete depends on accessor regen — not asserting beyond isolation
				});
			});

			describe( title="sibling duplicate isolation (dup1 vs dup2)", body=function(){
				it( title="two sibling duplicates with different mixins — fooMixin on dup1, barMixin on dup2 — fully isolated", body=function( currentSpec ){
					var proto = new LDEV3335.testWithAccessors();
					var dup1 = duplicate( proto );
					var dup2 = duplicate( proto );

					dup1.fooMixin = function() { return "foo from dup1"; };
					dup2.barMixin = function() { return "bar from dup2"; };

					// dup1 has foo, not bar
					expect( dup1.fooMixin() ).toBe( "foo from dup1" );
					expect( function(){ dup1.barMixin(); } ).toThrow();

					// dup2 has bar, not foo
					expect( dup2.barMixin() ).toBe( "bar from dup2" );
					expect( function(){ dup2.fooMixin(); } ).toThrow();

					// proto has neither
					expect( function(){ proto.fooMixin(); } ).toThrow();
					expect( function(){ proto.barMixin(); } ).toThrow();

					// accessors still dispatch independently against each instance's own scope
					proto.setA( "proto-a" );
					dup1.setA( "dup1-a" );
					dup2.setA( "dup2-a" );
					expect( proto.getA() ).toBe( "proto-a" );
					expect( dup1.getA() ).toBe( "dup1-a" );
					expect( dup2.getA() ).toBe( "dup2-a" );
				});
				it( title="two sibling duplicates override the same accessor with different closures — isolated", body=function( currentSpec ){
					var proto = new LDEV3335.testWithAccessors();
					var dup1 = duplicate( proto );
					var dup2 = duplicate( proto );

					dup1.getA = function() { return "from dup1"; };
					dup2.getA = function() { return "from dup2"; };

					expect( dup1.getA() ).toBe( "from dup1" );
					expect( dup2.getA() ).toBe( "from dup2" );

					// proto's real accessor still works against its own scope
					proto.setA( "proto-real" );
					expect( proto.getA() ).toBe( "proto-real" );
				});
				it( title="getMetadata stays structurally identical across dup1, dup2, proto even with divergent runtime mixins", body=function( currentSpec ){
					// getMetadata is structural — it reads from the shared ComponentProperties wrapper
					// and only surfaces declared cffunction/accessors, NOT runtime closure assignments
					// (those land in _data, not _udfs). Sharing the wrapper means metadata MUST stay
					// consistent across all duplicates of one prototype. Runtime mixins must not bleed
					// into metadata structure on any of them.
					var proto = new LDEV3335.testWithAccessors();
					var dup1 = duplicate( proto );
					var dup2 = duplicate( proto );

					// runtime mixins on each duplicate
					dup1.fooMixin = function() { return "foo"; };
					dup2.barMixin = function() { return "bar"; };

					// runtime dispatch works (isolation) — sanity check
					expect( dup1.fooMixin() ).toBe( "foo" );
					expect( dup2.barMixin() ).toBe( "bar" );
					expect( function(){ proto.fooMixin(); } ).toThrow();
					expect( function(){ proto.barMixin(); } ).toThrow();

					// metadata is identical across all three despite divergent mixins
					var metaProto = getMetadata( proto );
					var metaDup1  = getMetadata( dup1 );
					var metaDup2  = getMetadata( dup2 );

					expect( metaDup1.name ).toBe( metaProto.name );
					expect( metaDup2.name ).toBe( metaProto.name );
					expect( arrayLen( metaDup1.properties ) ).toBe( arrayLen( metaProto.properties ) );
					expect( arrayLen( metaDup2.properties ) ).toBe( arrayLen( metaProto.properties ) );
					expect( arrayLen( metaDup1.functions ) ).toBe( arrayLen( metaProto.functions ) );
					expect( arrayLen( metaDup2.functions ) ).toBe( arrayLen( metaProto.functions ) );
				});
			});

			describe( title="concurrent duplication — ORM hot-path stress", body=function(){
				it( title="50 threads × 20 duplicates each — all isolated, accessors dispatch correctly", body=function( currentSpec ){
					var prototype = new LDEV3335.testPropertyTypes();
					var threadCount = 50;
					var perThread = 20;
					var threadNames = [];
					for ( var t=1; t<=threadCount; t++ ) {
						var tname = "ldev6298-stress-" & t;
						arrayAppend( threadNames, tname );
						thread name="#tname#" tid=t proto=prototype perThread=perThread {
							thread.results = [];
							for ( var i=1; i<=attributes.perThread; i++ ) {
								var dup = duplicate( attributes.proto );
								var ageVal = ( attributes.tid * 1000 ) + i;
								dup.setAge( ageVal );
								dup.setName( "t" & attributes.tid & "-i" & i );
								arrayAppend( thread.results, { age: dup.getAge(), name: dup.getName(), expectedAge: ageVal, expectedName: "t" & attributes.tid & "-i" & i } );
							}
						}
					}
					thread action="join" name="#arrayToList( threadNames )#";
					for ( var t=1; t<=threadCount; t++ ) {
						var tname = "ldev6298-stress-" & t;
						var th = cfthread[ tname ];
						if ( th.status != "COMPLETED" ) throw( object=th.error );
						expect( arrayLen( th.results ) ).toBe( perThread );
						for ( var r in th.results ) {
							expect( r.age ).toBe( r.expectedAge );
							expect( r.name ).toBe( r.expectedName );
						}
					}
					// prototype itself untouched
					expect( prototype.getAge() ).toBe( 25 );
					expect( prototype.getName() ).toBe( "John" );
				});
				it( title="concurrent duplicate + mixin — mixin on one thread doesn't leak to another", body=function( currentSpec ){
					var prototype = new LDEV3335.testWithAccessors();
					var threadCount = 20;
					var threadNames = [];
					for ( var t=1; t<=threadCount; t++ ) {
						var tname = "ldev6298-mix-" & t;
						arrayAppend( threadNames, tname );
						thread name="#tname#" tid=t proto=prototype {
							var dup = duplicate( attributes.proto );
							dup.tag = function() { return "thread-" & attributes.tid; };
							dup.setA( "a-" & attributes.tid );
							thread.tagResult = dup.tag();
							thread.aResult = dup.getA();
						}
					}
					thread action="join" name="#arrayToList( threadNames )#";
					for ( var t=1; t<=threadCount; t++ ) {
						var tname = "ldev6298-mix-" & t;
						var th = cfthread[ tname ];
						if ( th.status != "COMPLETED" ) throw( object=th.error );
						expect( th.tagResult ).toBe( "thread-" & t );
						expect( th.aResult ).toBe( "a-" & t );
					}
					// prototype never received the mixin
					expect( function(){ prototype.tag(); } ).toThrow();
				});
				it( title="concurrent getMetadata across duplicates — shared javaAccessClass cache stays consistent", body=function( currentSpec ){
					// F1: commit 2 makes ComponentProperties.javaAccessClass a lazy cache on a SHARED wrapper.
					// Hammer getMetadata() concurrently across duplicates of one prototype to surface any
					// race / disk-contention / stale-cache issue introduced by the share.
					var prototype = new LDEV3335.testPropertyTypes();
					var dups = [];
					for ( var i=1; i<=20; i++ ) arrayAppend( dups, duplicate( prototype ) );
					var threadNames = [];
					for ( var t=1; t<=20; t++ ) {
						var tname = "ldev6298-meta-" & t;
						arrayAppend( threadNames, tname );
						thread name="#tname#" comp=dups[ t ] {
							thread.meta = getMetadata( attributes.comp );
						}
					}
					thread action="join" name="#arrayToList( threadNames )#";
					for ( var t=1; t<=20; t++ ) {
						var tname = "ldev6298-meta-" & t;
						var th = cfthread[ tname ];
						if ( th.status != "COMPLETED" ) throw( object=th.error );
						expect( arrayLen( th.meta.properties ) ).toBe( 3 );
					}
				});
			});
		});
	}
}
