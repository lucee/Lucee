component extends="org.lucee.cfml.test.LuceeTestCase"	{
	
	public void function testPostPlus(){
		var n=10;
		assertEquals(10,n++);
		assertEquals(11,n);
	}
	public void function testPostMinus(){
		var n=10;
		assertEquals(10,n--);
		assertEquals(9,n);
	}

	public void function testPrePlus(){
		var n=10;
		assertEquals(11,++n);
		assertEquals(11,n);
	}
	public void function testPreMinus(){
		var n=10;
		assertEquals(9,--n);
		assertEquals(9,n);
	}


} 



