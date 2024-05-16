component extends="org.lucee.cfml.test.LuceeTestCase"	{

	public void function testThread(){
		thread name="t3627" {
			thread.result=new LDEV3627.sub.Test().test();
		}
		thread action="join" name="t3627";
		assertEquals(
			"test",
			cfthread.t3627.result?:"undefined"
		);
	}
	
} 