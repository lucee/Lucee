component extends="org.lucee.cfml.test.LuceeTestCase" labels="threads" skip=true {

	function testThreadUnscopedVar () {
		thread name="thread-2773" {
			thread.test2773 = "2773";
			thread.inThread = isInThread();

			try {
				var test = test2773; // should resolve to thread scope
			} catch (e) {
				thread.error = e;
				// systemOutput( e );
			}
		}
		thread action="join" name="thread-2773";
		// systemOutput(cfthread["thread-2773"], true);
		expect ( cfthread["thread-2773"] ).toHaveKey( "inThread" );
		expect ( cfthread["thread-2773"].inThread ).toBeTrue( "isInThread() should be true" );
		
		expect ( cfthread["thread-2773"] ).toHaveKey( "test2773" );
		expect ( cfthread["thread-2773"].test2773 ).toBe( "2773" );
		if ( structKeyExists( cfthread["thread-2773"], "error" ) )
			fail( cfthread["thread-2773"].error );

	}
}