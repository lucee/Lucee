component extends="org.lucee.cfml.test.LuceeTestCase"	{

	public void function test(){

		var myStruct = { x = {y= { z=3.14 } }};

		assertEquals(3.14,structGet("myStruct.x.y.z"));
		assertEquals('{"Z":3.14}',serialize(structGet("myStruct.x.y")));
		assertEquals('{}',serialize(structGet("myStruct.z")));
		assertEquals('{}',serialize(myStruct.z));
		
	}

}
