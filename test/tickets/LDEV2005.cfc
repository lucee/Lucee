component extends="org.lucee.cfml.test.LuceeTestCase"	{

	public void function testGetCurrentTemplatePath(){
		assertEquals("LDEV2005.cfc",listLast(GetCurrentTemplatePath(),"\/"));
	}

	public void function testGetBaseTemplatePath(){
		assertEquals("TestBox.cfc",listLast(GetBaseTemplatePath(),"\/"));
	}

	public void function testGetTemplatePath(){
		var arr=GetTemplatePath();
		assertEquals("TestBox.cfc",listLast(arr[1],"\/"));
		assertEquals("LDEV2005.cfc",listLast(arr[arr.len()],"\/"));
	}
} 

 