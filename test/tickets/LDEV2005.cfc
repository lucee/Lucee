component extends="org.lucee.cfml.test.LuceeTestCase"	{

	public void function testGetCurrentTemplatePath(){
		assertEquals("LDEV2005.cfc",listLast(GetCurrentTemplatePath(),"\/"));
	}

	public void function testGetBaseTemplatePath(){
		assertEquals("run-tests.cfm",listLast(GetBaseTemplatePath(),"\/"));
	}

	public void function testGetTemplatePath(){
		var arr=GetTemplatePath();
		assertEquals("_testRunner.cfc",listLast(arr[2],"\/"));
		assertEquals("LDEV2005.cfc",listLast(arr[arr.len()],"\/"));
	}
} 

 