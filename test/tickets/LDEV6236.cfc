component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults, testBox ){
		describe( title="LDEV-6236 share UDFGSProperty in addUDFS no-owner branch + callDirect bypass", body=function(){

			describe( title="accessor isolation across createObject instances (no-owner branch)", body=function(){
				it( title="50 fresh instances — property values isolated", body=function( currentSpec ){
					var instances = [];
					for ( var i=1; i<=50; i++ ) {
						var inst = new LDEV3335.testPropertyTypes();
						inst.setAge( i );
						inst.setName( "inst-" & i );
						arrayAppend( instances, inst );
					}
					for ( var i=1; i<=50; i++ ) {
						expect( instances[ i ].getAge() ).toBe( i );
						expect( instances[ i ].getName() ).toBe( "inst-" & i );
					}
				});
				it( title="mixin on one fresh instance doesn't leak to siblings", body=function( currentSpec ){
					var a = new LDEV3335.testWithAccessors();
					var b = new LDEV3335.testWithAccessors();
					var c = new LDEV3335.testWithAccessors();
					b.injected = function() { return "from b"; };
					expect( b.injected() ).toBe( "from b" );
					expect( function(){ a.injected(); } ).toThrow();
					expect( function(){ c.injected(); } ).toThrow();
				});
				it( title="closure override of accessor on one instance doesn't leak to siblings", body=function( currentSpec ){
					var a = new LDEV3335.testPropertyTypes();
					var b = new LDEV3335.testPropertyTypes();
					a.setAge( 11 );
					b.setAge( 22 );
					b.getAge = function() { return 999; };
					expect( a.getAge() ).toBe( 11 );
					expect( b.getAge() ).toBe( 999 );
				});
				it( title="structDelete of override on one instance doesn't affect siblings", body=function( currentSpec ){
					// Accessor regen on the instance after structDelete is not guaranteed
					// (see LDEV6298.cfc:148) — only isolation across siblings is asserted here.
					var a = new LDEV3335.testPropertyTypes();
					var b = new LDEV3335.testPropertyTypes();
					a.setAge( 11 );
					b.setAge( 22 );
					b.getAge = function() { return 999; };
					expect( b.getAge() ).toBe( 999 );
					structDelete( b, "getAge" );
					// sibling untouched
					expect( a.getAge() ).toBe( 11 );
					// b can still write via setter; sibling still untouched
					b.setAge( 33 );
					expect( a.getAge() ).toBe( 11 );
				});
			});

			describe( title="callDirect bypass — accessor returns property value, not the UDF", body=function(){
				it( title="generated getter returns the underlying value", body=function( currentSpec ){
					var cfc = new LDEV3335.testPropertyTypes();
					cfc.setAge( 42 );
					var v = cfc.getAge();
					expect( v ).toBe( 42 );
					expect( isCustomFunction( v ) ).toBeFalse();
				});
				it( title="setter returns the component for chaining", body=function( currentSpec ){
					var cfc = new LDEV3335.testPropertyTypes();
					var ret = cfc.setAge( 30 );
					expect( ret.getAge() ).toBe( 30 );
				});
				it( title="typed casting still applies through callDirect path", body=function( currentSpec ){
					var cfc = new LDEV3335.testPropertyTypes();
					cfc.setAge( "42" );
					expect( cfc.getAge() ).toBe( 42 );
					expect( isNumeric( cfc.getAge() ) ).toBeTrue();
				});
			});

			describe( title="concurrent createObject + accessor dispatch (shared accessor stress)", body=function(){
				it( title="50 threads × 20 fresh instances — fully isolated property values", body=function( currentSpec ){
					var threadCount = 50;
					var perThread = 20;
					var threadNames = [];
					for ( var t=1; t<=threadCount; t++ ) {
						var tname = "ldev6236-stress-" & t;
						arrayAppend( threadNames, tname );
						thread name="#tname#" tid=t perThread=perThread {
							thread.results = [];
							for ( var i=1; i<=attributes.perThread; i++ ) {
								var inst = new LDEV3335.testPropertyTypes();
								var ageVal = ( attributes.tid * 1000 ) + i;
								inst.setAge( ageVal );
								inst.setName( "t" & attributes.tid & "-i" & i );
								arrayAppend( thread.results, { age: inst.getAge(), name: inst.getName(), expectedAge: ageVal, expectedName: "t" & attributes.tid & "-i" & i } );
							}
						}
					}
					thread action="join" name="#arrayToList( threadNames )#";
					for ( var t=1; t<=threadCount; t++ ) {
						var tname = "ldev6236-stress-" & t;
						var th = cfthread[ tname ];
						if ( th.status != "COMPLETED" ) throw( object=th.error );
						expect( arrayLen( th.results ) ).toBe( perThread );
						for ( var r in th.results ) {
							expect( r.age ).toBe( r.expectedAge );
							expect( r.name ).toBe( r.expectedName );
						}
					}
				});
				it( title="concurrent new + mixin injection — no cross-thread leak", body=function( currentSpec ){
					var threadCount = 20;
					var threadNames = [];
					for ( var t=1; t<=threadCount; t++ ) {
						var tname = "ldev6236-mix-" & t;
						arrayAppend( threadNames, tname );
						thread name="#tname#" tid=t {
							var inst = new LDEV3335.testWithAccessors();
							inst.tag = function() { return "thread-" & attributes.tid; };
							inst.setA( "a-" & attributes.tid );
							thread.tagResult = inst.tag();
							thread.aResult = inst.getA();
						}
					}
					thread action="join" name="#arrayToList( threadNames )#";
					for ( var t=1; t<=threadCount; t++ ) {
						var tname = "ldev6236-mix-" & t;
						var th = cfthread[ tname ];
						if ( th.status != "COMPLETED" ) throw( object=th.error );
						expect( th.tagResult ).toBe( "thread-" & t );
						expect( th.aResult ).toBe( "a-" & t );
					}
				});
			});

		});
	}
}
