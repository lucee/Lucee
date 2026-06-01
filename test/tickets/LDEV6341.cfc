component extends="org.lucee.cfml.test.LuceeTestCase" labels="classLoader" {

	function run( testResults, testBox ) {

		describe( "LDEV-6341 restore thread CCL on register/release", function() {

			describe( "ThreadLocalConfig", function() {

				it( "restores CCL on release", function() {
					inIsolatedScope( function( sentinel, pc, config ) {
						var thread = getThread();
						var TLC = getTLC();

						TLC.register( config );
						// register installed the Lucee CCL — not the sentinel
						expect( sentinel.equals( thread.getContextClassLoader() ) ).toBeFalse( "register should have installed Lucee CCL, but sentinel is still active" );

						TLC.release();
						// release must restore the captured sentinel
						expect( sentinel.equals( thread.getContextClassLoader() ) ).toBeTrue( "CCL not restored to sentinel" );
					});
				});

				it( "re-entrant register preserves the original CCL save", function() {
					// guards the Controler.run pattern: register(config) called 4x before
					// the single release(). Without the re-entrancy guard, the second
					// register would clobber the original save with the Lucee CCL.
					inIsolatedScope( function( sentinel, pc, config ) {
						var thread = getThread();
						var TLC = getTLC();

						TLC.register( config );
						TLC.register( config );
						TLC.register( config );
						TLC.register( config );
						TLC.release();

						expect( sentinel.equals( thread.getContextClassLoader() ) ).toBeTrue( "CCL not restored to sentinel" );
					});
				});

				it( "register(null) does not mutate CCL", function() {
					inIsolatedScope( function( sentinel, pc, config ) {
						var thread = getThread();
						var TLC = getTLC();

						TLC.register( javacast( "null", "" ) );
						// existing contract: register(null) is a no-op for the CCL
						expect( sentinel.equals( thread.getContextClassLoader() ) ).toBeTrue( "CCL not restored to sentinel" );

						TLC.release();
						expect( sentinel.equals( thread.getContextClassLoader() ) ).toBeTrue( "CCL not restored to sentinel" );
					});
				});

				it( "release without prior register is a safe no-op for CCL", function() {
					inIsolatedScope( function( sentinel, pc, config ) {
						var thread = getThread();
						var TLC = getTLC();

						TLC.release();
						expect( sentinel.equals( thread.getContextClassLoader() ) ).toBeTrue( "CCL not restored to sentinel" );
					});
				});
			});

			describe( "ThreadLocalPageContext", function() {

				it( "register/release restores CCL", function() {
					inIsolatedScope( function( sentinel, pc, config ) {
						var thread = getThread();
						var TLPC = getTLPC();

						TLPC.register( pc );
						expect( sentinel.equals( thread.getContextClassLoader() ) ).toBeFalse( "register should have installed Lucee CCL, but sentinel is still active" );

						TLPC.release();
						expect( sentinel.equals( thread.getContextClassLoader() ) ).toBeTrue( "CCL not restored to sentinel" );
					});
				});

				it( "registerChild restores CCL on release", function() {
					inIsolatedScope( function( sentinel, pc, config ) {
						var thread = getThread();
						var TLPC = getTLPC();

						TLPC.registerChild( pc );
						expect( sentinel.equals( thread.getContextClassLoader() ) ).toBeFalse( "register should have installed Lucee CCL, but sentinel is still active" );

						TLPC.release();
						expect( sentinel.equals( thread.getContextClassLoader() ) ).toBeTrue( "CCL not restored to sentinel" );
					});
				});

				it( "re-entrant register preserves the original CCL save", function() {
					inIsolatedScope( function( sentinel, pc, config ) {
						var thread = getThread();
						var TLPC = getTLPC();

						TLPC.register( pc );
						TLPC.register( pc );
						TLPC.release();

						expect( sentinel.equals( thread.getContextClassLoader() ) ).toBeTrue( "CCL not restored to sentinel" );
					});
				});
			});

			describe( "composition", function() {

				it( "nested ThreadLocalConfig + ThreadLocalPageContext compose correctly in reverse-order release", function() {
					// spec contract: independent prevCCL slots, releases must be reverse-order
					// to traverse cleanly back to the pre-first-register CCL.
					inIsolatedScope( function( sentinel, pc, config ) {
						var thread = getThread();
						var TLC = getTLC();
						var TLPC = getTLPC();

						TLC.register( config );
						TLPC.register( pc );

						// reverse-order release
						TLPC.release();
						TLC.release();

						expect( sentinel.equals( thread.getContextClassLoader() ) ).toBeTrue( "CCL not restored to sentinel" );
					});
				});

				it( "out-of-order release leaves CCL stuck at an intermediate state (by-design contract)", function() {
					// Spec contract: each register API maintains its OWN prevCCL slot.
					// Releasing the outer (TLC) before the inner (TLPC) means TLC's
					// release restores to sentinel, then TLPC's release restores to
					// the value it captured (Lucee CCL from TLC.register). Final CCL
					// is stuck on Lucee CCL.
					//
					// This test does not assert what we WANT; it pins what we GET, so
					// a future change that "fixes" out-of-order release (e.g. by
					// coupling the two slots into a stack) fails this test loudly and
					// the contract change is deliberate.
					inIsolatedScope( function( sentinel, pc, config ) {
						var thread = getThread();
						var TLC = getTLC();
						var TLPC = getTLPC();

						TLC.register( config );
						TLPC.register( pc );

						// out-of-order release — outer first
						TLC.release();
						TLPC.release();

						expect( sentinel.equals( thread.getContextClassLoader() ) ).toBeFalse(
							"out-of-order release should leave CCL stuck at the intermediate (Lucee) state — if this test passes, the contract has changed and the spec's reverse-order rule no longer holds"
						);
					});
				});
			});

			describe( "contract", function() {

				it( "CCL restored when an exception escapes the register/release window in try/finally", function() {
					// The fix relies on callers using try { register; ... } finally { release; }.
					// Verify the pattern survives an exception thrown inside the work block —
					// without this, an exception path could silently leave a leaked CCL.
					inIsolatedScope( function( sentinel, pc, config ) {
						var thread = getThread();
						var TLC = getTLC();
						var caught = false;

						try {
							TLC.register( config );
							expect( sentinel.equals( thread.getContextClassLoader() ) ).toBeFalse();
							throw( type="LDEV6341.DeliberateForTest", message="exception inside work unit" );
						} catch ( "LDEV6341.DeliberateForTest" e ) {
							caught = true;
						} finally {
							TLC.release();
						}

						expect( caught ).toBeTrue( "test setup: exception should have been caught" );
						expect( sentinel.equals( thread.getContextClassLoader() ) ).toBeTrue( "CCL not restored after exception in work unit" );
					});
				});

				it( "prevCCL is per-thread — child cfthread register/release does not pollute parent's saved CCL", function() {
					// Plain ThreadLocal<SavedCCL> (not InheritableThreadLocal) means each
					// thread has its own prevCCL slot. A child doing its own register/release
					// must not affect the parent's saved CCL — otherwise the parent's
					// subsequent release would restore the wrong value.
					inIsolatedScope( function( sentinel, pc, config ) {
						var thread = getThread();
						var TLC = getTLC();

						TLC.register( config );
						// parent state now: prevCCL_TLC = sentinel, CCL = Lucee

						thread name="ldev6341child" priority="low" {
							var tlcChild = createObject( "java", "lucee.runtime.engine.ThreadLocalConfig" );
							var pcChild = getPageContext();
							// child's prevCCL_TLC slot is empty (per-thread, not inherited);
							// child's register captures the child's own current CCL.
							tlcChild.register( pcChild.getConfig() );
							tlcChild.release();
						}
						thread action="join" name="ldev6341child";

						// Despite the child's independent register/release, parent's saved
						// CCL is intact — release must restore the sentinel.
						TLC.release();
						expect( sentinel.equals( thread.getContextClassLoader() ) ).toBeTrue( "child cfthread's register/release should not have polluted parent's saved CCL" );
					});
				});
			});

		});
	}

	private any function getThread() {
		return createObject( "java", "java.lang.Thread" ).currentThread();
	}

	private any function getTLC() {
		return createObject( "java", "lucee.runtime.engine.ThreadLocalConfig" );
	}

	private any function getTLPC() {
		return createObject( "java", "lucee.runtime.engine.ThreadLocalPageContext" );
	}

	private any function makeSentinelCL() {
		// AppClassLoader/system CL — known-distinct from Lucee's EnvClassLoader,
		// stable across test runs.
		return createObject( "java", "java.lang.ClassLoader" ).getSystemClassLoader();
	}

	// Test isolation harness.
	//
	// Each test runs in a clean slate: TLC and TLPC slots cleared (so the test's
	// own register call is the first one to capture prevCCL), CCL set to a sentinel
	// ClassLoader. After the test, restores the request's PC + Lucee CCL so the
	// surrounding test harness keeps working.
	//
	// Asymmetry: only the PC slot is re-registered in the finally. TLC's
	// cThreadLocal is naturally null for the request thread (request entry doesn't
	// call ThreadLocalConfig.register — only ThreadLocalPageContext.register), so
	// leaving it cleared is the right post-state.
	//
	// Body receives ( sentinelClassLoader, pc, config ).
	private void function inIsolatedScope( required function body ) {
		var thread = getThread();
		var TLC = getTLC();
		var TLPC = getTLPC();
		var pc = getPageContext();
		var config = pc.getConfig();
		var prev = thread.getContextClassLoader();

		// Clear any pre-existing thread-local state so our register is the first one
		// (request entry already called TLPC.register, populating prevCCL_TLPC).
		TLC.release();
		TLPC.release();

		var sentinel = makeSentinelCL();
		thread.setContextClassLoader( sentinel );

		try {
			arguments.body( sentinel, pc, config );
		} finally {
			// Restore CCL first so re-registering the PC doesn't see the sentinel.
			thread.setContextClassLoader( prev );
			// Restore the test harness's PC so subsequent tests work.
			TLPC.register( pc );
		}
	}
}
