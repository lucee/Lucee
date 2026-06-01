component extends="org.lucee.cfml.test.LuceeTestCase" labels="xml" {
	function run( testResults , testBox ) {
		describe( "test case for Duplicate", function() {
			it(title = "Checking with Duplicate", body = function( currentSpec ) {
		 		cfapplication (action="update" clientmanagement="true");
				<!--- begin old test code --->
				<!--- String --->
					var str="String";
					variables.test={};
					var str2=duplicate(str);
					var str="String 2";
					assertEquals("String", "#str2#");
				<!--- Number --->
					str=1+1;
					str2=duplicate(str);
					str=str+1;
					assertEquals("2", "#str2#");
				<!--- boolean --->
					str=true;
					str2=duplicate(str);
					str=false;
					assertEquals("true", "#str2#");
				<!--- struct --->
					str=structNew();
					str.data="aaaaa";
					str2=duplicate(str);
					str.data="bbbbb";
					assertEquals("aaaaa", "#str2.data#");
				<!--- array --->
					str=arrayNew(1);
					str[1]="aaaaa";
					str2=duplicate(str);
					str[1]="bbbbb";
					assertEquals("aaaaa", "#str2[1]#");
				<!--- query --->
					var qry=queryNew("col");
					QueryAddRow(qry);
					QuerySetCell(qry,"col","aaaaa");
					var qry2=duplicate(qry);
					QuerySetCell(qry,"col","bbbbb");
					assertEquals("aaaaa", "#qry2.col#");
				if(server.ColdFusion.ProductName eq "RAILO"){
					cfobject(type="component",name="c",component="duplicate.comps.some.Hello");
					assertEquals("0", "#c.get()#");
					var d=duplicate(c);
					local.c.set(1);
					assertEquals("1", "#c.get()#");
					assertEquals("0", "#d.get()#");
				}
				<!--- not working in JSR223env --->
				if(server.lucee.environment=="servlet"){
					duplicate(client);
					duplicate(session);
					duplicate(application);
					duplicate(cgi);
				}
				duplicate(request);
				// duplicate(variables);
				duplicate(server);

				savecontent variable="local.xrds"{
					writeOutput('<?xml version="1.0" encoding="UTF-8"?><xrd><Service priority="10"><Type>http://openid.net/signon/1.0</Type><URI priority="15">http://resolve2.example.com</URI><URI priority="10">http://resolve.example.com</URI><URI>https://resolve.example.com</URI></Service></xrd>');
				}
				var xrds = xmlParse(trim(xrds)).xmlRoot;
				var xrdsService = xrds.xmlChildren[1];
				xrdsService.xmlChildren[2] = duplicate(xrdsService.URI[2]);
				xrdsService.URI[1] = duplicate(xrdsService.URI[2]);
				<!--- end old test code --->
			});

			describe( "duplicate of a CFC — per-instance state isolation", function(){
				it( title="cfc.variables.x assigned then duplicated — duplicate has independent value", body=function( currentSpec ){
					var orig = new Duplicate.Tagged();
					orig.variables.adhoc = "orig-adhoc";
					var dup = duplicate( orig );
					expect( dup.variables.adhoc ).toBe( "orig-adhoc" );
					dup.variables.adhoc = "dup-adhoc";
					expect( orig.variables.adhoc ).toBe( "orig-adhoc" );
					expect( dup.variables.adhoc ).toBe( "dup-adhoc" );
				});
				it( title="cfc.this.x assigned then duplicated — duplicate has independent value", body=function( currentSpec ){
					var orig = new Duplicate.Tagged();
					orig.adhoc = "orig-this";
					var dup = duplicate( orig );
					expect( dup.adhoc ).toBe( "orig-this" );
					dup.adhoc = "dup-this";
					expect( orig.adhoc ).toBe( "orig-this" );
					expect( dup.adhoc ).toBe( "dup-this" );
				});
			});

			describe( "ThreadLocalDuplication re-entry (self-referential / cyclic CFCs)", function(){
				// Identity is asserted via mutation-through-reference rather than equality compare,
				// because TestBox's toBe() recurses through CFC content and stack-overflows on cycles.
				it( title="duplicate of CFC holding reference to itself doesn't infinite-loop", body=function( currentSpec ){
					var c = new Duplicate.Tagged( tag="root" );
					c.self = c;
					var d = duplicate( c );
					expect( d.tag ).toBe( "root" );
					d.marker = "before";
					d.self.marker = "via-self";
					expect( d.marker ).toBe( "via-self" );
					expect( structKeyExists( c, "marker" ) ).toBeFalse();
				});
				it( title="duplicate of two-CFC cycle (a.peer = b, b.peer = a) — both duplicates form a cycle of their own", body=function( currentSpec ){
					var a = new Duplicate.Tagged( tag="a" );
					var b = new Duplicate.Tagged( tag="b" );
					a.peer = b;
					b.peer = a;
					var dupA = duplicate( a );
					expect( dupA.tag ).toBe( "a" );
					expect( dupA.peer.tag ).toBe( "b" );
					dupA.peer.peer.marker = "via-cycle";
					expect( dupA.marker ).toBe( "via-cycle" );
					expect( structKeyExists( a, "marker" ) ).toBeFalse();
					expect( structKeyExists( b, "marker" ) ).toBeFalse();
				});
				it( title="concurrent self-referential duplicates — ThreadLocal scoped per thread", body=function( currentSpec ){
					var threadCount = 20;
					var threadNames = [];
					for ( var t=1; t<=threadCount; t++ ) {
						var tname = "duplicate-reentry-" & t;
						arrayAppend( threadNames, tname );
						thread name="#tname#" tid=t {
							var c = new Duplicate.Tagged( tag="t-" & attributes.tid );
							c.self = c;
							var d = duplicate( c );
							d.self.marker = "m-" & attributes.tid;
							thread.tagOut = d.tag;
							thread.markerOut = d.marker;
							thread.sourceUntouched = !structKeyExists( c, "marker" );
						}
					}
					thread action="join" name="#arrayToList( threadNames )#";
					for ( var t=1; t<=threadCount; t++ ) {
						var tname = "duplicate-reentry-" & t;
						var th = cfthread[ tname ];
						if ( th.status != "COMPLETED" ) throw( object=th.error );
						expect( th.tagOut ).toBe( "t-" & t );
						expect( th.markerOut ).toBe( "m-" & t );
						expect( th.sourceUntouched ).toBeTrue();
					}
				});
			});

			describe( "duplicate of CFC with accessors — runtime mutation isolation", function(){
				it( title="overriding accessor on duplicate doesn't leak to source", body=function( currentSpec ){
					var orig = new test.general.accessors.testPropertyTypes();
					orig.setAge( 30 );
					var copy = duplicate( orig );
					copy.getAge = function() { return 999; };
					expect( copy.getAge() ).toBe( 999 );
					expect( orig.getAge() ).toBe( 30 );
				});
				it( title="overriding accessor on source doesn't leak to existing duplicate", body=function( currentSpec ){
					var orig = new test.general.accessors.testPropertyTypes();
					var copy = duplicate( orig );
					orig.getAge = function() { return 777; };
					expect( orig.getAge() ).toBe( 777 );
					copy.setAge( 42 );
					expect( copy.getAge() ).toBe( 42 );
				});
				it( title="injecting new method on duplicate doesn't leak to source", body=function( currentSpec ){
					var orig = new test.general.accessors.testWithAccessors();
					var copy = duplicate( orig );
					copy.customMethod = function() { return "from copy"; };
					expect( copy.customMethod() ).toBe( "from copy" );
					expect( function(){ orig.customMethod(); } ).toThrow();
				});
			});

			describe( "mass duplication (ORM hot path)", function(){
				it( title="50 duplicates from one prototype — all isolated", body=function( currentSpec ){
					var prototype = new test.general.accessors.testPropertyTypes();
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
					var prototype = new test.general.accessors.testWithAccessors();
					prototype.setA( "before" );
					var early = duplicate( prototype );
					prototype.setA( "after" );
					var late = duplicate( prototype );
					expect( early.getA() ).toBe( "before" );
					expect( late.getA() ).toBe( "after" );
				});
			});

			describe( "chained duplicates (a → b → c)", function(){
				it( title="three-level chain — each level isolated", body=function( currentSpec ){
					var a = new test.general.accessors.testWithAccessors();
					a.setA( "level-a" );
					var b = duplicate( a );
					b.setA( "level-b" );
					var c = duplicate( b );
					c.setA( "level-c" );
					expect( a.getA() ).toBe( "level-a" );
					expect( b.getA() ).toBe( "level-b" );
					expect( c.getA() ).toBe( "level-c" );
				});
				it( title="mixin at level B + mixin at level C — each visible only at and below its level", body=function( currentSpec ){
					var a = new test.general.accessors.testWithAccessors();
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

			describe( "sibling duplicate isolation (dup1 vs dup2)", function(){
				it( title="two sibling duplicates with different mixins — fully isolated", body=function( currentSpec ){
					var proto = new test.general.accessors.testWithAccessors();
					var dup1 = duplicate( proto );
					var dup2 = duplicate( proto );

					dup1.fooMixin = function() { return "foo from dup1"; };
					dup2.barMixin = function() { return "bar from dup2"; };

					expect( dup1.fooMixin() ).toBe( "foo from dup1" );
					expect( function(){ dup1.barMixin(); } ).toThrow();

					expect( dup2.barMixin() ).toBe( "bar from dup2" );
					expect( function(){ dup2.fooMixin(); } ).toThrow();

					expect( function(){ proto.fooMixin(); } ).toThrow();
					expect( function(){ proto.barMixin(); } ).toThrow();

					proto.setA( "proto-a" );
					dup1.setA( "dup1-a" );
					dup2.setA( "dup2-a" );
					expect( proto.getA() ).toBe( "proto-a" );
					expect( dup1.getA() ).toBe( "dup1-a" );
					expect( dup2.getA() ).toBe( "dup2-a" );
				});
				it( title="two sibling duplicates override the same accessor with different closures — isolated", body=function( currentSpec ){
					var proto = new test.general.accessors.testWithAccessors();
					var dup1 = duplicate( proto );
					var dup2 = duplicate( proto );

					dup1.getA = function() { return "from dup1"; };
					dup2.getA = function() { return "from dup2"; };

					expect( dup1.getA() ).toBe( "from dup1" );
					expect( dup2.getA() ).toBe( "from dup2" );

					proto.setA( "proto-real" );
					expect( proto.getA() ).toBe( "proto-real" );
				});
				it( title="getMetadata stays structurally identical across dup1, dup2, proto despite divergent runtime mixins", body=function( currentSpec ){
					// Runtime closure assignments land in _data, not _udfs, so the shared
					// ComponentProperties wrapper must surface identical metadata across
					// all duplicates of one prototype.
					var proto = new test.general.accessors.testWithAccessors();
					var dup1 = duplicate( proto );
					var dup2 = duplicate( proto );

					dup1.fooMixin = function() { return "foo"; };
					dup2.barMixin = function() { return "bar"; };

					expect( dup1.fooMixin() ).toBe( "foo" );
					expect( dup2.barMixin() ).toBe( "bar" );
					expect( function(){ proto.fooMixin(); } ).toThrow();
					expect( function(){ proto.barMixin(); } ).toThrow();

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

			describe( "concurrent duplication — ORM hot-path stress", function(){
				it( title="50 threads × 20 duplicates each — all isolated, accessors dispatch correctly", body=function( currentSpec ){
					var prototype = new test.general.accessors.testPropertyTypes();
					var threadCount = 50;
					var perThread = 20;
					var threadNames = [];
					for ( var t=1; t<=threadCount; t++ ) {
						var tname = "duplicate-stress-" & t;
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
						var tname = "duplicate-stress-" & t;
						var th = cfthread[ tname ];
						if ( th.status != "COMPLETED" ) throw( object=th.error );
						expect( arrayLen( th.results ) ).toBe( perThread );
						for ( var r in th.results ) {
							expect( r.age ).toBe( r.expectedAge );
							expect( r.name ).toBe( r.expectedName );
						}
					}
					expect( prototype.getAge() ).toBe( 25 );
					expect( prototype.getName() ).toBe( "John" );
				});
				it( title="concurrent duplicate + mixin — mixin on one thread doesn't leak to another", body=function( currentSpec ){
					var prototype = new test.general.accessors.testWithAccessors();
					var threadCount = 20;
					var threadNames = [];
					for ( var t=1; t<=threadCount; t++ ) {
						var tname = "duplicate-mix-" & t;
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
						var tname = "duplicate-mix-" & t;
						var th = cfthread[ tname ];
						if ( th.status != "COMPLETED" ) throw( object=th.error );
						expect( th.tagResult ).toBe( "thread-" & t );
						expect( th.aResult ).toBe( "a-" & t );
					}
					expect( function(){ prototype.tag(); } ).toThrow();
				});
				it( title="concurrent getMetadata across duplicates — shared javaAccessClass cache stays consistent", body=function( currentSpec ){
					// LDEV-6298: ComponentProperties.javaAccessClass is a lazy cache on a
					// shared wrapper post-fix; concurrent getMetadata across many duplicates
					// must not race or surface a stale-cache inconsistency.
					var prototype = new test.general.accessors.testPropertyTypes();
					var dups = [];
					for ( var i=1; i<=20; i++ ) arrayAppend( dups, duplicate( prototype ) );
					var threadNames = [];
					for ( var t=1; t<=20; t++ ) {
						var tname = "duplicate-meta-" & t;
						arrayAppend( threadNames, tname );
						thread name="#tname#" comp=dups[ t ] {
							thread.meta = getMetadata( attributes.comp );
						}
					}
					thread action="join" name="#arrayToList( threadNames )#";
					for ( var t=1; t<=20; t++ ) {
						var tname = "duplicate-meta-" & t;
						var th = cfthread[ tname ];
						if ( th.status != "COMPLETED" ) throw( object=th.error );
						expect( arrayLen( th.meta.properties ) ).toBe( 3 );
					}
				});
			});

		});
	}
}