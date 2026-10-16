component extends="org.lucee.cfml.test.LuceeTestCase" labels="thread" {

	function run( testResults, testBox ) {
		describe( "IsThreadInterrupted and ThreadInterrupted", function() {

			it( "IsThreadInterrupted returns false for non-interrupted thread", function() {
				var threadName = "ldev5339_notInterrupted_#createUUID()#";
				thread name=threadName {
					sleep( 500 );
				}
				sleep( 50 ); // give thread time to start
				expect( isThreadInterrupted( threadName ) ).toBeFalse();
				thread action="join" name=threadName;
			});

			it( "IsThreadInterrupted returns true for interrupted thread", function() {
				var threadName = "ldev5339_interrupted_#createUUID()#";
				thread name=threadName {
					sleep( 5000 );
				}
				sleep( 50 ); // give thread time to start
				thread action="interrupt" name=threadName;
				expect( isThreadInterrupted( threadName ) ).toBeTrue();
				// calling again should still be true (flag not cleared)
				expect( isThreadInterrupted( threadName ) ).toBeTrue();
				thread action="join" name=threadName;
			});

			it( "IsThreadInterrupted throws for non-existent thread", function() {
				expect( function() {
					isThreadInterrupted( "nonExistentThread_#createUUID()#" );
				}).toThrow();
			});

			it( "ThreadInterrupted returns false for non-interrupted current thread", function() {
				var threadName = "ldev5339_currentNotInterrupted_#createUUID()#";
				var result = {};
				thread name=threadName result=result {
					attributes.result.wasInterrupted = threadInterrupted();
				}
				thread action="join" name=threadName;
				expect( result.wasInterrupted ).toBeFalse();
			});

			it( "ThreadInterrupted returns true and clears flag for interrupted current thread", function() {
				var threadName = "ldev5339_currentInterrupted_#createUUID()#";
				var result = { started: false };
				thread name=threadName result=result {
					// Busy-wait loop checking for interrupt (no sleep to avoid InterruptedException)
					attributes.result.started = true;
					var loopCount = 0;
					var startTime = getTickCount();
					// Loop for max 5 seconds or until interrupted
					while ( !threadInterrupted() && ( getTickCount() - startTime ) < 5000 ) {
						loopCount++;
						// Busy wait - just spin
					}
					attributes.result.firstCheck = true; // we exited loop
					// calling again should be false (flag was cleared by threadInterrupted)
					attributes.result.secondCheck = threadInterrupted();
				}
				// Wait for thread to start
				var waitStart = getTickCount();
				while ( !result.started && ( getTickCount() - waitStart ) < 1000 ) {
					sleep( 10 );
				}
				sleep( 50 ); // give thread time to enter loop
				thread action="interrupt" name=threadName;
				thread action="join" name=threadName;
				expect( result.firstCheck ).toBeTrue();
				expect( result.secondCheck ).toBeFalse();
			});

		});
	}
}
