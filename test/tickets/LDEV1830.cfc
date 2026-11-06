component extends="org.lucee.cfml.test.LuceeTestCase"{
	

	public void function testSaveLoad(){
		local.x=new LDEV1830.ClosureFactory().createClosure();
		local.os=objectSave(x);
		local.o=objectLoad(os);

		assertEquals("abcdef",x());
		assertEquals("abcdef",o());
	}

	public void function testSaveLoadInner() {

		local.inner=new LDEV1830.ClosureFactory().createInner();

		assertEquals("abcdef",inner());
	}

}