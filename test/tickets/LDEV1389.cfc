component extends="org.lucee.cfml.test.LuceeTestCase"{
	

	public void function testThread(){
		local.names="";
		local.max=20;
		loop from=1 to=max index="local.i" {
			local.name="ldev1389-#i#";
			names=listAppend(names,name);
			thread name=name action="run" args={} {
				Thread.searchArgs={};
				Thread.stResult=1;
				Thread.done=true;

			}
		}
		thread action="join" name="#names#";

		loop list=names item="name" {
			assertEquals("completed",cfthread[name].status);
			assertTrue(cfthread[name].done);
		}
		
	}

	public void function testThreadHTTP(){
		thread name="testthread" {
			thread.data=1;
			http url="https://extension.lucee.org/rest/extension/provider/info" result="http";
			thread.data=1;
		}
		thread action="join" name="testthread";
		assertEquals("completed",cfthread.testthread.STATUS);
	}
}