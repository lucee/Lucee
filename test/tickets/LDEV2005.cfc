component extends="org.lucee.cfml.test.LuceeTestCase"	{

	public void function testGetCurrentTemplatePath(){
		assertEquals("LDEV2005.cfc",listLast(GetCurrentTemplatePath(),"\/"));
	}

	public void function testGetBaseTemplatePath(){
		assertEquals("_testRunner.cfc",listLast(GetBaseTemplatePath(),"\/"));
	}

	public void function testGetTemplatePath(){
		var arr=GetTemplatePath();
		assertEquals("TestBox.cfc",listLast(arr[2],"\/"));
		assertEquals("LDEV2005.cfc",listLast(arr[arr.len()],"\/"));
	}
} 

 