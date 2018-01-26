component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( "test case for LDEV-380", function() {
			it(title = "Checking cfthread action = 'join', from another a thread", body = function( currentSpec ) {
				testThread();
				expect(variables.hasError).toBe("");
			});
		});

		private void function testThread(){
			variables.hasError = "";
			thread name="T1" action="run"{
				sleep(1000);
			}
			thread name="T2" action="run"{
				try{
					thread action="join" name="T1" timeout="5000";
				} catch( any e){
					variables.hasError = e.message;
				}
			}
			writeOutput(variables.hasError);
		}
	}
}