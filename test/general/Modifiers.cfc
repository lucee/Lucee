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
	
		var funcs={};
		loop array=meta.functions item="local.sct" {
			funcs[sct.name]=sct;
		}
		
		var m=funcs.final;
		assertEquals("package",m.access);
		assertEquals(true,m.output);
		assertEquals("final",m.name);
		assertEquals("whatever",m.returntype);
		assertEquals("final",m.modifier); 

		var m=funcs.public;
		assertEquals("package",m.access);
		assertEquals(true,m.output);
		assertEquals("public",m.name);
		assertEquals("whatever",m.returntype);
		assertEquals("final",m.modifier);  

		var m=funcs.package;
		assertEquals("package",m.access);
		assertEquals(true,m.output);
		assertEquals("package",m.name);
		assertEquals("whatever",m.returntype);
		assertEquals("final",m.modifier); 

		var m=funcs.private;
		assertEquals("package",m.access);
		assertEquals(true,m.output);
		assertEquals("private",m.name);
		assertEquals("function",m.returntype);
		assertEquals("final",m.modifier); 

		var m=funcs.test;
		assertEquals("package",m.access);
		assertEquals(true,m.output);
		assertEquals("test",m.name);
		assertEquals("private",m.returntype);
		assertEquals("final",m.modifier); 

	}

} 