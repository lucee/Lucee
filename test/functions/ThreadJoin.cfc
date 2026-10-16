component extends="org.lucee.cfml.test.LuceeTestCase" labels="thread"{
    
    function beforeAll() {
        variables.threadPrefix = "testThread_" & createUUID();
    }
    
    function run( testResults , testBox ) {
        describe("ThreadJoin Function", function() {
            
            it("joins a single thread and verifies completion", function() {
                // Create a thread
                var threadName = variables.threadPrefix & "_single";
                thread name=threadName action="run" {
                    sleep(50); // Simulate work
                    thread.res = "Thread completed successfully";
                }
                
                // Join the thread
                threadJoin(threadName);
                var result = cfthread[threadName];
                systemOutput(result,1,1);
				expect(result.STATUS).toBe("COMPLETED");
                expect(result.res).toBe("Thread completed successfully");
            });
        });
    }
}