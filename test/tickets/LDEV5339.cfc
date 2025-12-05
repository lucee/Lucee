component extends="org.lucee.cfml.test.LuceeTestCase" skip="true"{
	
	function run( testResults , testBox ) {
		describe( "Testcase for LDEV-5339", function() {
			it( title="should detect when a thread has been interrupted using isThreadInterrupted()", body=function( currentSpec ) {
                var threadName = "LDEV5339_test";
                var result = "";
                try{
                    thread action="run" name=threadName{ 
                        sleep(50);
                    }
                    sleep(50);
                    // interrupt the thread
                    thread action="interrupt" name=threadName{
                    }
                    //check if the thread is interrupted
                   result = isThreadInterrupted( threadName );
                   
                }catch (any error) {
                    result = error.message;
                }
                expect( cfthread[ threadName ].INTERRUPTED ).toBeTrue( );
				expect( result ).toBe( 'true' );   
			});
		});
	}
    
}
