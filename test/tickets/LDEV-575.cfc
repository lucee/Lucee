/**
* Create a new function: isInThread() to allow for checking if you are in a thread or not
*/
component extends="testbox.system.BaseSpec"{
	
/*********************************** LIFE CYCLE Methods ***********************************/

	// executes before all suites+specs in the run() method
	function beforeAll(){
	}

	// executes after all suites+specs in the run() method
	function afterAll(){
	}

/*********************************** BDD SUITES ***********************************/

	function run( testResults, testBox ){
		// all your suites go here.
		story( "Provide a way to verify if am in a cfthread or not.", function(){
			given( "I am NOT in a thread", function(){
				then( "the result should be false", function(){
					expect(	isInThread() ).toBeFalse();
				});
			});
			given( "I am in a thread", function(){
				then( "the result should be true", function(){
					callThread();
					expect(	request.data ).toBeTrue();
				});
			
			});
		});
	}

	// Workaround until compiler issue is solved
	function callThread(){
		thread name="threadTest"{
			request.data = isInThread();
		}
		thread action="join" names="threadTest";
	}
	
}