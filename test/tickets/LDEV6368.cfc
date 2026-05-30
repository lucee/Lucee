component extends="org.lucee.cfml.test.LuceeTestCase" labels="thread" {

	function run( testResults, testBox ) {
		describe( "LDEV-6368 cfthread virtual attribute", function() {

			it( "creates a virtual thread and populates the thread scope", function() {
				var tn = "ldev6368_basic_#createUUID()#";
				thread name=tn virtual=true {
					thread.value = 42;
				}
				thread action="join" name=tn;
				expect( cfthread[ tn ].status ).toBe( "COMPLETED" );
				expect( cfthread[ tn ].value ).toBe( 42 );
				// virtual threads require Java 21+, on older JVMs it falls back to a platform thread
				expect( cfthread[ tn ].virtual ).toBe( javaVersion() >= 21 );
			});

			it( "exposes virtual=false in the thread scope for a platform thread", function() {
				var tn = "ldev6368_scopeflag_#createUUID()#";
				thread name=tn {
					thread.value = 1;
				}
				thread action="join" name=tn;
				expect( cfthread[ tn ].virtual ).toBeFalse();
			});

			it( "runs on a real Java virtual thread when supported by the JVM", function() {
				if ( javaVersion() < 21 ) return; // virtual threads require Java 21+
				var tn = "ldev6368_isvirtual_#createUUID()#";
				thread name=tn virtual=true {
					thread.onVirtual = createObject( "java", "java.lang.Thread" ).currentThread().isVirtual();
				}
				thread action="join" name=tn;
				expect( cfthread[ tn ].onVirtual ).toBeTrue();
			});

			it( "uses a platform thread by default", function() {
				if ( javaVersion() < 21 ) return;
				var tn = "ldev6368_platform_#createUUID()#";
				thread name=tn {
					thread.onVirtual = createObject( "java", "java.lang.Thread" ).currentThread().isVirtual();
				}
				thread action="join" name=tn;
				expect( cfthread[ tn ].onVirtual ).toBeFalse();
			});

			it( "supports join on a virtual thread", function() {
				var tn = "ldev6368_join_#createUUID()#";
				thread name=tn virtual=true {
					sleep( 200 );
					thread.done = true;
				}
				thread action="join" name=tn;
				expect( cfthread[ tn ].status ).toBe( "COMPLETED" );
				expect( cfthread[ tn ].done ).toBeTrue();
			});

			it( "supports interrupt on a virtual thread", function() {
				var tn = "ldev6368_interrupt_#createUUID()#";
				thread name=tn virtual=true {
					sleep( 5000 );
				}
				sleep( 100 ); // give thread time to start
				thread action="interrupt" name=tn;
				expect( isThreadInterrupted( tn ) ).toBeTrue();
				thread action="join" name=tn;
			});

			it( "supports terminate on a virtual thread", function() {
				var tn = "ldev6368_terminate_#createUUID()#";
				thread name=tn virtual=true {
					sleep( 5000 );
				}
				sleep( 100 ); // give thread time to start
				thread action="terminate" name=tn;
				thread action="join" name=tn timeout=2000;
				expect( cfthread[ tn ].status ).toBe( "TERMINATED" );
			});

		});
	}

	private numeric function javaVersion() {
		return val( listFirst( server.java.version, "._-" ) );
	}
}
