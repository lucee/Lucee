component extends="org.lucee.cfml.test.LuceeTestCase" labels="logging" {
	function run( testResults , testBox ) {
		describe(title="Testcase for LDEV-4128", body=function() {
			it(title="checking cflog async is faster than non-async", body=function( currentSpec ) {
				
				var logname = "test-#createUniqueID()#"; // create a new log for each run avoid pool problem
				var times = 10000;

				timer variable="local.nonAsync" {
					loop times=times {
						cflog(file="#logname#" text="load test", type="error", async=false);
					}
				}

				timer variable="local.async" {
					loop times=times {
						cflog(file="#logname#-async" text="load test", type="error", async=true);
					}
				}

				systemOutput("---- Async: #async# vs nonAsync #nonAsync# ---" , true) ;

				expect( async ).toBeLT( nonAsync );
			});
		});
	}
}