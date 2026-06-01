component extends="org.lucee.cfml.test.LuceeTestCase" {

	// Generic CFC inheritance contracts:
	//   - super.method() through 3 levels
	//   - `this` resolves to the calling instance from inherited methods
	//   - property pulldown via accessors at every level (mappedSuperClass shape)
	//   - exception thrown from a base UDF references the calling instance
	//   - concurrent createObject + concurrent super.X() under stress

	function run( testResults, testBox ){
		describe( "CFC inheritance", function(){

			describe( "3-level super dispatch", function(){
				it( title="super.super chain returns each level joined", body=function( currentSpec ){
					var c = new inheritance.Child();
					expect( c.chain() ).toBe( "grand:parent:child" );
				});
				it( title="duplicate of a 3-level child preserves super chain", body=function( currentSpec ){
					var c = new inheritance.Child();
					var d = duplicate( c );
					expect( d.chain() ).toBe( "grand:parent:child" );
				});
				it( title="50 sequential new Child() — every chain() resolves identically", body=function( currentSpec ){
					for ( var i=1; i<=50; i++ ) {
						var c = new inheritance.Child();
						expect( c.chain() ).toBe( "grand:parent:child" );
					}
				});
			});

			describe( "`this` inside inherited method resolves to calling child", function(){
				it( title="whoAmI() defined on Grandparent reads child's tag", body=function( currentSpec ){
					var c = new inheritance.Child( tag="alpha" );
					expect( c.whoAmI() ).toBe( "alpha" );
				});
				it( title="two siblings of same class — each whoAmI() returns own tag", body=function( currentSpec ){
					var a = new inheritance.Child( tag="aa" );
					var b = new inheritance.Child( tag="bb" );
					expect( a.whoAmI() ).toBe( "aa" );
					expect( b.whoAmI() ).toBe( "bb" );
				});
				it( title="duplicate then mutate tag — whoAmI() reflects the duplicate's tag", body=function( currentSpec ){
					var orig = new inheritance.Child( tag="orig" );
					var dup = duplicate( orig );
					dup.tag = "dup";
					expect( orig.whoAmI() ).toBe( "orig" );
					expect( dup.whoAmI() ).toBe( "dup" );
				});
				it( title="tagMe() inherited from Grandparent writes to calling child's scope", body=function( currentSpec ){
					var a = new inheritance.Child( tag="a-init" );
					var b = new inheritance.Child( tag="b-init" );
					a.tagMe( "a-after" );
					expect( a.whoAmI() ).toBe( "a-after" );
					expect( b.whoAmI() ).toBe( "b-init" );
				});
			});

			describe( "exception thrown from base UDF references calling instance", function(){
				it( title="boom() inherited from Boom — error message includes Child's tag", body=function( currentSpec ){
					var c = new inheritance.BoomChild( tag="explode" );
					var caught = "";
					var caughtType = "";
					try {
						c.trigger();
					} catch ( any e ) {
						caught = e.message;
						caughtType = e.type;
					}
					expect( caughtType ).toBe( "LDEV6300.Boom" );
					expect( caught ).toBe( "boom from explode" );
				});
				it( title="two siblings throwing — each sees its own tag in the error", body=function( currentSpec ){
					var a = new inheritance.BoomChild( tag="aa" );
					var b = new inheritance.BoomChild( tag="bb" );
					var msgA = "";
					var msgB = "";
					try { a.trigger(); } catch ( any e ) { msgA = e.message; }
					try { b.trigger(); } catch ( any e ) { msgB = e.message; }
					expect( msgA ).toBe( "boom from aa" );
					expect( msgB ).toBe( "boom from bb" );
				});
			});

			describe( "multi-level property pulldown (3-level extends, mappedSuperClass shape)", function(){
				it( title="3-level extends — accessors at every level dispatch on the leaf", body=function( currentSpec ){
					var c = new inheritance.Child();
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
					var c = new inheritance.Child();
					c.setGprop( "g1" );
					c.setPprop( "p1" );
					c.setCprop( "c1" );
					var d = duplicate( c );
					expect( d.getGprop() ).toBe( "g1" );
					expect( d.getPprop() ).toBe( "p1" );
					expect( d.getCprop() ).toBe( "c1" );
					d.setGprop( "g2" );
					d.setPprop( "p2" );
					d.setCprop( "c2" );
					expect( c.getGprop() ).toBe( "g1" );
					expect( c.getPprop() ).toBe( "p1" );
					expect( c.getCprop() ).toBe( "c1" );
				});
			});

			describe( "concurrent createObject of same class (init-side stress)", function(){
				it( title="50 threads × 20 fresh Child() — chain + whoAmI consistent per thread", body=function( currentSpec ){
					var threadCount = 50;
					var perThread = 20;
					var threadNames = [];
					for ( var t=1; t<=threadCount; t++ ) {
						var tname = "inheritance-init-" & t;
						arrayAppend( threadNames, tname );
						thread name="#tname#" tid=t perThread=perThread {
							thread.results = [];
							for ( var i=1; i<=attributes.perThread; i++ ) {
								var tag = "t" & attributes.tid & "-i" & i;
								var c = new inheritance.Child( tag=tag );
								arrayAppend( thread.results, { chain: c.chain(), who: c.whoAmI(), expectedTag: tag } );
							}
						}
					}
					thread action="join" name="#arrayToList( threadNames )#";
					for ( var t=1; t<=threadCount; t++ ) {
						var tname = "inheritance-init-" & t;
						var th = cfthread[ tname ];
						if ( th.status != "COMPLETED" ) throw( object=th.error );
						expect( arrayLen( th.results ) ).toBe( perThread );
						for ( var r in th.results ) {
							expect( r.chain ).toBe( "grand:parent:child" );
							expect( r.who ).toBe( r.expectedTag );
						}
					}
				});
				it( title="concurrent new while siblings mutate `this` scope — no cross-thread bleed", body=function( currentSpec ){
					var threadCount = 30;
					var perThread = 10;
					var threadNames = [];
					for ( var t=1; t<=threadCount; t++ ) {
						var tname = "inheritance-scope-" & t;
						arrayAppend( threadNames, tname );
						thread name="#tname#" tid=t perThread=perThread {
							thread.results = [];
							for ( var i=1; i<=attributes.perThread; i++ ) {
								var c = new inheritance.Child( tag="t" & attributes.tid & "-" & i );
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
						var tname = "inheritance-scope-" & t;
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

			describe( "getMetaData reads from inherited dispatch", function(){
				// Grandparent.metaSnapshot() calls getMetaData(this) — when dispatched on
				// a Child, every field below must read the calling LEAF's properties
				// (e.g. "Child"), not Grandparent's.
				it( title="metaSnapshot dispatched from Grandparent reads Child's metadata", body=function( currentSpec ){
					var c = new inheritance.Child();
					var snap = c.metaSnapshot();
					expect( right( snap.name, len( ".inheritance.Child" ) ) ).toBe( ".inheritance.Child" );
					expect( right( snap.fullname, len( ".inheritance.Child" ) ) ).toBe( ".inheritance.Child" );
					expect( right( snap.extends, len( ".inheritance.Parent" ) ) ).toBe( ".inheritance.Parent" );
					expect( snap.accessors ).toBeTrue();
				});
				it( title="metaSnapshot after duplicate still reads Child's metadata", body=function( currentSpec ){
					var c = new inheritance.Child();
					var d = duplicate( c );
					var snap = d.metaSnapshot();
					expect( right( snap.name, len( ".inheritance.Child" ) ) ).toBe( ".inheritance.Child" );
					expect( right( snap.extends, len( ".inheritance.Parent" ) ) ).toBe( ".inheritance.Parent" );
					expect( snap.accessors ).toBeTrue();
				});
				it( title="getMetaData on leaf surfaces inherited properties from every level", body=function( currentSpec ){
					var c = new inheritance.Child();
					var meta = getMetaData( c );
					var propNames = [];
					for ( var p in meta.properties ) arrayAppend( propNames, p.name );
					expect( propNames ).toInclude( "cprop" );
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
				it( title="fresh new vs duplicate of new — JSON-serialised metadata is identical", body=function( currentSpec ){
					var orig = new inheritance.Child();
					var dup = duplicate( orig );
					expect( serializeJSON( getMetaData( orig ) ) ).toBe( serializeJSON( getMetaData( dup ) ) );
				});
			});

			describe( "child inherits parent accessors", function(){
				it( title="child inherits parent property accessors", body=function( currentSpec ){
					var child = new inheritance.testInheritChild();
					expect(child.getParentProp()).toBe("from parent");
				});
				it( title="child manual method overrides parent accessor", body=function( currentSpec ){
					var child = new inheritance.testInheritChildOverride();
					expect(child.getParentProp()).toBe("CHILD OVERRIDE");
				});
				it( title="duplicate() with inheritance — child accessors work", body=function( currentSpec ){
					var child = new inheritance.testInheritChild();
					var copy = duplicate( child );
					expect( copy.getParentProp() ).toBe( "from parent" );
					copy.setParentProp( "modified" );
					expect( copy.getParentProp() ).toBe( "modified" );
					expect( child.getParentProp() ).toBe( "from parent" );
				});
				it( title="duplicate() with child override — override preserved", body=function( currentSpec ){
					var orig = new inheritance.testInheritChildOverride();
					var copy = duplicate( orig );
					expect( copy.getParentProp() ).toBe( "CHILD OVERRIDE" );
				});
			});

			describe( "concurrent super dispatch", function(){
				it( title="100 threads × super.chain() — every result is `grand:parent:child`", body=function( currentSpec ){
					var threadCount = 100;
					var threadNames = [];
					for ( var t=1; t<=threadCount; t++ ) {
						var tname = "inheritance-superrace-" & t;
						arrayAppend( threadNames, tname );
						thread name="#tname#" tid=t {
							var c = new inheritance.Child( tag="t-" & attributes.tid );
							thread.chain = c.chain();
							thread.tag = c.tag;
						}
					}
					thread action="join" name="#arrayToList( threadNames )#";
					for ( var t=1; t<=threadCount; t++ ) {
						var tname = "inheritance-superrace-" & t;
						var th = cfthread[ tname ];
						if ( th.status != "COMPLETED" ) throw( object=th.error );
						expect( th.chain ).toBe( "grand:parent:child" );
						expect( th.tag ).toBe( "t-" & t );
					}
				});
			});

		});
	}
}
