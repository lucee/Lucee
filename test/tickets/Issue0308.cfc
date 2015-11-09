component extends="org.lucee.cfml.test.LuceeTestCase"	{
	
	public void function testStaticConstructorLifeCycle(){ 
		// reset the static scope
		file action="touch" file="Issue0308/StaticConstructorLifeCycle.cfc";

		assertEquals("0-0",Issue0308.StaticConstructorLifeCycle::getCount());
		new Issue0308.StaticConstructorLifeCycle();
		assertEquals("1-1",Issue0308.StaticConstructorLifeCycle::getCount());
		new Issue0308.StaticConstructorLifeCycle();
		assertEquals("2-2",Issue0308.StaticConstructorLifeCycle::getCount());
	}

} 



