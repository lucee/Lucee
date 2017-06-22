component extends="org.lucee.cfml.test.LuceeTestCase"{
	

	public void function testThread(){
		thread name="ldev1389" action="run" {
			Thread.searchArgs={};
			Thread.stResult=1;
			Thread.done=true;

		}
		thread action="join" name="ldev1389";
		assertEquals("completed",cfthread.ldev1389.status);
		assertTrue(cfthread.ldev1389.done);
	}
}