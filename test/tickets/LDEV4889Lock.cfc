component extends="org.lucee.cfml.test.LuceeTestCase" labels="lock" {

	function run( testResults, testBox ) {
		variables.adminPassword = request.ServerAdminPassword;
		describe( "LDEV-4889: cflock must release its active lock entry exactly once", function() {

			it( "a lock timeout throws a lock exception, not an EmptyStackException", function() {
				var name = "LDEV4889-timeout-#createUUID()#";
				_holdLock( name, function() {
					var err = "";
					try {
						lock name="#name#" type="exclusive" timeout="1" {
							fail( "the lock should not have been granted" );
						}
					}
					catch ( any e ) {
						err = e;
					}
					expect( err ).notToBe( "", "expected a lock timeout" );
					expect( err.type ).toBe( "lock" );
					expect( err.message ).notToInclude( "EmptyStackException" );
				});
			});

			it( "a lock timeout with throwontimeout=false continues without an error", function() {
				var name = "LDEV4889-nothrow-#createUUID()#";
				_holdLock( name, function() {
					var entered = false;
					lock name="#name#" type="exclusive" timeout="1" throwontimeout="false" {
						entered = true;
					}
					expect( entered ).toBeFalse();
				});
			});

			it( "a lock timeout inside another lock does not release the outer lock entry", function() {
				var outer = "LDEV4889-outer-#createUUID()#";
				var inner = "LDEV4889-inner-#createUUID()#";
				_holdLock( inner, function() {
					lock name="#outer#" type="exclusive" timeout="5" {
						lock name="#inner#" type="exclusive" timeout="1" throwontimeout="false" {
							fail( "the lock should not have been granted" );
						}
						// the outer lock has to be listed as active still
						var pc = getPageContext();
						expect( arrayLen( pc.getActiveLocks() ) ).toBe( 1 );
						expect( pc.getActiveLocks()[ 1 ].name ).toBe( outer );
					}
				});
			});

			it( "a lock with an invalid attribute combination reports that error, not an EmptyStackException", function() {
				var err = "";
				try {
					lock name="LDEV4889-invalid" scope="request" type="exclusive" timeout="1" {
						fail( "should not run" );
					}
				}
				catch ( any e ) {
					err = e;
				}
				expect( err ).notToBe( "", "expected an error" );
				expect( err.message ).notToInclude( "EmptyStackException" );
			});

			it( "an error while releasing the lock is reported as is, not replaced by an EmptyStackException", function() {
				// a monitor whose class cannot be loaded makes the action monitor collector fail when the lock logs its release,
				// the class of this monitor is only available through the loadPaths of the registrar, not in this test
				var monitorName = "LDEV4889-broken-monitor";
				var err = "";
				var lockName = "LDEV4889-broken-#createUUID()#";
				var activeLocks = -1;
				var regained = false;
				new _LDEV4889MonitorRegistrar().register( monitorName, variables.adminPassword );
				try {
					_getConfigServer().resetMonitors();
					try {
						_returnInsideLock( lockName );
					}
					catch ( any e ) {
						err = e;
					}
					activeLocks = arrayLen( getPageContext().getActiveLocks() );
				}
				finally {
					admin action="removeMonitor" type="server" password=variables.adminPassword monitorType="request" name=monitorName;
					_getConfigServer().resetMonitors();
				}
				// the lock must have been given back
				lock name="#lockName#" type="exclusive" timeout="1" {
					regained = true;
				}
				expect( err ).notToBe( "", "the failure to load the monitor should have been reported" );
				expect( err.message ).notToInclude( "EmptyStackException" );
				expect( err.message ).toInclude( "BlockingRequestMonitor" );
				expect( activeLocks ).toBe( 0 );
				expect( regained ).toBeTrue();
			});

			it( "return from every case of a switch inside a lock releases the lock once", function() {
				for ( var type in [ "fail", "pass", "error", "skipped" ] ) {
					var r = _incStat( type );
					expect( r ).toBe( type );
				}
			});

		});
	}

	private string function _incStat( required string type ) {
		lock name="LDEV4889-switch-#createUUID()#" type="exclusive" timeout="10" {
			switch ( arguments.type ) {
				case "fail": { return "fail"; }
				case "pass": { return "pass"; }
				case "error": { return "error"; }
				case "skipped": { return "skipped"; }
			}
		}
		return "none";
	}

	private string function _returnInsideLock( required string name ) {
		lock name="#arguments.name#" type="exclusive" timeout="10" {
			return "done";
		}
	}

	private function _getConfigServer() {
		return createObject( "java", "lucee.runtime.config.ConfigUtil" ).getConfigServerImpl( getPageContext().getConfig() );
	}

	// holds the lock with the given name in another thread for the duration of the callback
	private function _holdLock( required string name, required function callback ) {
		var holder = "LDEV4889hold#createUUID().replace( '-', '', 'all' )#";
		var started = createObject( "java", "java.util.concurrent.CountDownLatch" ).init( 1 );
		var release = createObject( "java", "java.util.concurrent.CountDownLatch" ).init( 1 );
		thread name="#holder#" lockName="#arguments.name#" started="#started#" release="#release#" {
			lock name="#attributes.lockName#" type="exclusive" timeout="30" {
				attributes.started.countDown();
				attributes.release.await( 30, createObject( "java", "java.util.concurrent.TimeUnit" ).SECONDS );
			}
		}
		try {
			started.await( 10, createObject( "java", "java.util.concurrent.TimeUnit" ).SECONDS );
			arguments.callback();
		}
		finally {
			release.countDown();
			thread action="join" name="#holder#" timeout="10000";
		}
	}

}
