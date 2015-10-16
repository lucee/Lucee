component extends="org.lucee.cfml.test.LuceeTestCase"	{
	
	//public function beforeTests(){}
	
	//public function afterTests(){}
	
	//public function setUp(){}


	public void function testNoModifiers(){
		var meta=getMetaData(new modifiers.NoModifiers());
		meta=meta.functions[1];
		assertEquals("public",meta.access);
		assertEquals("test",meta.name);
		assertEquals(true,meta.output);
		assertEquals("any",meta.returntype);
		assertEquals("",meta.modifier);
	}

	public void function testReturnType(){
		var meta=getMetaData(new modifiers.ReturnType());
		meta=meta.functions[1];
		assertEquals("public",meta.access);
		assertEquals("test",meta.name);
		assertEquals(true,meta.output);
		assertEquals("whatever",meta.returntype);
		assertEquals("",meta.modifier);
	}

	public void function testAccessModifier(){
		var meta=getMetaData(new modifiers.AccessModifier());
		meta=meta.functions[1];
		assertEquals("private",meta.access);
		assertEquals("test",meta.name);
		assertEquals(true,meta.output);
		assertEquals("any",meta.returntype);
		assertEquals("",meta.modifier);
	}

	public void function testNonAccessModifier(){
		var meta=getMetaData(new modifiers.NonAccessModifier());
		meta=meta.functions[1];
		assertEquals("public",meta.access);
		assertEquals("test",meta.name);
		assertEquals(true,meta.output);
		assertEquals("any",meta.returntype);
		assertEquals("final",meta.modifier);
	}

	public void function testModifier(){
		var meta=getMetaData(new modifiers.Modifier());
		meta1=meta.functions[1];
		meta2=meta.functions[2];
		
		assertEquals("package",meta1.access);
		assertEquals("test1",meta1.name);
		assertEquals(true,meta1.output);
		assertEquals("any",meta1.returntype);
		assertEquals("final",meta1.modifier); 

		assertEquals("package",meta2.access);
		assertEquals("test2",meta2.name);
		assertEquals(true,meta2.output);
		assertEquals("any",meta2.returntype);
		assertEquals("final",meta2.modifier); 
	}



	public void function testAll(){
		var meta=getMetaData(new modifiers.All());
		meta1=meta.functions[1];
		meta2=meta.functions[2];
		meta3=meta.functions[3];
		meta4=meta.functions[4];
		meta5=meta.functions[5];
		
		assertEquals("package",meta1.access);
		assertEquals(true,meta1.output);
		assertEquals("final",meta1.name);
		assertEquals("whatever",meta1.returntype);
		assertEquals("final",meta1.modifier); 

		assertEquals("package",meta2.access);
		assertEquals(true,meta2.output);
		assertEquals("public",meta2.name);
		assertEquals("whatever",meta2.returntype);
		assertEquals("final",meta2.modifier);  

		assertEquals("package",meta3.access);
		assertEquals(true,meta3.output);
		assertEquals("package",meta3.name);
		assertEquals("whatever",meta3.returntype);
		assertEquals("final",meta3.modifier); 

		assertEquals("package",meta3.access);
		assertEquals(true,meta3.output);
		assertEquals("package",meta3.name);
		assertEquals("whatever",meta3.returntype);
		assertEquals("final",meta3.modifier);  

		assertEquals("package",meta4.access);
		assertEquals(true,meta4.output);
		assertEquals("private",meta4.name);
		assertEquals("function",meta4.returntype);
		assertEquals("final",meta4.modifier); 

		assertEquals("package",meta5.access);
		assertEquals(true,meta5.output);
		assertEquals("test",meta5.name);
		assertEquals("private",meta5.returntype);
		assertEquals("final",meta5.modifier); 

	}

} 