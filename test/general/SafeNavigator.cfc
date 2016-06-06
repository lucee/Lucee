component extends="org.lucee.cfml.test.LuceeTestCase"	{


	public void function testSyntax() {
		var z=a?.b?.c?.d;
		var z=a()?.b?.c?.d;
		var z=a?.b()?.c?.d;
		var z=a?.b?.c()?.d;
		var z=a?.b?.c?.d();
		var z=a?.b()?.c?.d();
		var z=a()?.b()?.c()?.d();
	}

	public void function test() {
		var a.b.c.d=1;

		assertEquals(1,a.b.c.d&"");
		assertEquals(1,a?.b?.c?.d&"");
		assertEquals('',a?.b?.c?.d0&"");
		assertEquals('',a?.b?.c0?.d&"");
		assertEquals('',a?.b0?.c?.d&"");
		assertEquals('',a0?.b?.c?.d&"");
	}


	public void function testChildNotExist() {
		try {
			assertEquals('',a?.b?.c.d&"");
			fail("this should fail");
		}
		catch(local.e) {}
	}

	public void function testparentNotExist1() {
		try {
			assertEquals('',a.b?.c?.d&"");
			fail("this should fail");
		}
		catch(local.e) {}
	}

	public void function testparentNotExist2() {
		try {
			assertEquals('',a.b.c?.d&"");
			fail("this should fail");
		}
		catch(local.e) {}
	}


}