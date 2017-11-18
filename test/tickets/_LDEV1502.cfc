component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( "test case for LDEV-1502", function() {
			it(title = "Checking cfthread, without thread initialize", body = function( currentSpec ) {
				assertEquals(true, isStruct(cfthread));
				assertEquals(true, StructisEmpty(cfthread));
			});

			it(title = "Checking cfthread, after thread initialize", body = function( currentSpec ) {
				testThread();
				assertEquals(true, isStruct(cfthread));
				assertEquals("COMPLETED", cfthread.LDEV1502.status);
			});
		});
	}

	private void function testThread(){
		thread name="LDEV1502" action="run"{
			Thread.foo="bar";
		}
		thread action="join" name="LDEV1502";
	}
}