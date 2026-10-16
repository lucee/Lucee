component extends="org.lucee.cfml.test.LuceeTestCase"	{

	public void function testThread(){
		thread name="t3627" {
			thread.result=new LDEV3627.sub.Test().test();
		}
		thread action="join" name="t3627";
		assertEquals(
			"test",
			cfthread.t3627.result?:"undefined"
		);
	}

	// parallel arrayMap clones the PageContext via UDFCaller2 (different path than cfthread)
	// each closure must still resolve LDEV3627.sub.Test relative to this test file
	public void function testParallelMap(){
		var results = arrayMap( [ 1, 2, 3, 4, 5 ], function( item ){
			return new LDEV3627.sub.Test().test();
		}, true );
		assertEquals( 5, arrayLen( results ) );
		for ( var r in results ) {
			assertEquals( "test", r );
		}
	}

	// clone-of-a-clone: cfthread clones the test PC, then parallel arrayMap inside the thread
	// clones the thread's PC. Inner closure must transitively still resolve the relative CFC.
	public void function testNestedParallelInThread(){
		thread name="t3627_nested" {
			thread.results = arrayMap( [ 1, 2, 3 ], function( item ){
				return new LDEV3627.sub.Test().test();
			}, true );
		}
		thread action="join" name="t3627_nested";
		var results = cfthread.t3627_nested.results ?: [];
		assertEquals( 3, arrayLen( results ) );
		for ( var r in results ) {
			assertEquals( "test", r );
		}
	}

	// inverse nesting: parallel arrayMap clones first, then each closure spawns a cfthread
	// the inner cfthread clones from the parallel clone. Same transitive requirement.
	public void function testThreadInParallel(){
		var results = arrayMap( [ 1, 2 ], function( item ){
			var tname = "t3627_inner_" & item;
			thread name="#tname#" {
				thread.r = new LDEV3627.sub.Test().test();
			}
			thread action="join" name="#tname#";
			return cfthread[ tname ].r ?: "undefined";
		}, true );
		assertEquals( 2, arrayLen( results ) );
		for ( var r in results ) {
			assertEquals( "test", r );
		}
	}

	// cfinclude with a relative path inside a parallel closure exercises getRealPageSource(realPath)
	// from a clone PC, which the LDEV-3689 parent-walk workaround did NOT cover.
	public void function testParallelInclude(){
		var results = arrayMap( [ 1, 2, 3 ], function( item ){
			var out = "";
			savecontent variable="out" {
				include "LDEV3627/sub/snippet.cfm";
			}
			return trim( out );
		}, true );
		assertEquals( 3, arrayLen( results ) );
		for ( var r in results ) {
			assertEquals( "included", r );
		}
	}

	// cfmodule template= with a relative path inside a parallel closure. Same untested reader,
	// different tag entry point (Module.java -> getRelativePageSources).
	public void function testParallelModule(){
		var results = arrayMap( [ 1, 2, 3 ], function( item ){
			var out = "";
			savecontent variable="out" {
				cfmodule( template="LDEV3627/sub/module-target.cfm", value=item );
			}
			return trim( out );
		}, true );
		assertEquals( 3, arrayLen( results ) );
		for ( var i = 1; i <= 3; i++ ) {
			assertEquals( "module:" & i, results[ i ] );
		}
	}

}